/**
 * Author:  Alexander
 * Created: 30.03.2018
 */

/* Alter the akasha_task table to store a cron expression */

alter table akasha_task
    add column repeat_cron      varchar(64)             null default null
;