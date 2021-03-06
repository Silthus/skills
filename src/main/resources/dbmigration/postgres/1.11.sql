-- apply changes
alter table rcs_player_bindings drop constraint if exists ck_rcs_player_bindings_material;
alter table rcs_player_bindings alter column material type varchar(255) using material::varchar(255);
alter table rcs_player_bindings drop constraint if exists ck_rcs_player_bindings_action;
alter table rcs_player_bindings alter column action type varchar(11) using action::varchar(11);
alter table rcs_player_bindings add constraint ck_rcs_player_bindings_action check ( action in ('RIGHT_CLICK','LEFT_CLICK'));
