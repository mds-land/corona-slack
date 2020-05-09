create table tokens
(
	team_id varchar(20) not null,
	token varchar(255) not null
);

create unique index TOKENS_TEAM_ID_UINDEX
	on tokens (team_id);

alter table tokens
	add constraint TOKENS_PK
		primary key (team_id);

