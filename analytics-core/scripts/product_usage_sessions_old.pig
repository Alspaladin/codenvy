/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

IMPORT 'macros.pig';

t = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
f = productUsageTimeList(t, '10');

result = FOREACH f GENERATE ToMilliSeconds(dt), TOTUPLE('user', user), TOTUPLE('value', delta);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

r1 = FOREACH f GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, id, delta;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain),
            TOTUPLE('session_id', id), TOTUPLE('start_time', ToString(dt, 'yyyy-MM-dd HH:mm:ss')),
            TOTUPLE('end_time', ToString(ToDate(ToMilliSeconds(dt) + delta * 1000), 'yyyy-MM-dd HH:mm:ss')),
            TOTUPLE('value', delta);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE-raw' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

---------------------------------------
-- USERS: The number of sessions
---------------------------------------
k1 = LOAD '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoLoader('$STORAGE_USER', '$STORAGE_PASSWORD', 'id: chararray, sessions: Long');
k = FOREACH k1 GENERATE id, (sessions IS NULL ? 0 : sessions) AS sessions;

-- calculate total user's sessions
m1 = GROUP f BY user;
m2 = FOREACH m1 GENERATE group AS id, COUNT(f) AS sessions;
m = FILTER m2 BY INDEXOF(UPPER(id), 'ANONYMOUSUSER_', 0) != 0 AND id != 'default';

--combine and store result
n1 = JOIN m BY id LEFT, k BY id;
n2 = FOREACH n1 GENERATE k::id AS id, (m::sessions + (k::sessions IS NULL ? 0 : k::sessions)) AS sessions;
n = FOREACH n2 GENERATE id, TOTUPLE('sessions', sessions);
STORE n INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');
