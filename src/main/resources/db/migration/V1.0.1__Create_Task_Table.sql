/**
 * Author:  Alexander
 * Created: 26.03.2018
 */

create table akasha_task (
    id                      bigint                      not null auto_increment ,
    user_id                 bigint                      not null                ,
    task_name               varchar(255)                not null                ,
    task_type               int                         not null                ,
    task_status             int                         not null                ,
    task_priority           int                         not null                ,
    period_seconds          int                         not null                ,
    description             varchar(4096)               null default null       ,
    deadline                timestamp                   null default null       ,
    start_time              time                        null default null       ,
    primary key ( id )                                                          ,
    foreign key ( user_id ) references akasha_user ( id )                       ,
    unique key uidx_task_name ( task_name )
)
;
