-- apply changes
alter table rcs_player_skills add column disabled tinyint(1) default 0 not null;

