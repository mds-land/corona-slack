create table team_config
(
    team_id varchar(20) not null,
    config VARCHAR(10000) not null,
    enabled BOOLEAN not null
);

create unique index TEAM_CONFIG_TEAM_ID_UINDEX
    on team_config (team_id);

alter table team_config
    add constraint TEAM_CONFIG_PK
        primary key (team_id);

