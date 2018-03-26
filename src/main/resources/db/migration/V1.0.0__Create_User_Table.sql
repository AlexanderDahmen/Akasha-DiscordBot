/**
 * Author:  Alexander
 * Created: 26.03.2018
 */

create table akasha_user (
    id                      bigint                      not null                ,
    user_name               varchar(255)                not null                ,
    channel_id              bigint                      not null default 0      ,
    primary key ( id )
)
;
