create table report_count (
    id bigint not null auto_increment,
    count integer not null,
    resource_simple_class_name_and_resource_name varchar(255) not null,
    report_execution_id bigint not null,
    primary key (id)
) ENGINE=InnoDB;

create table report_execution (
    id bigint not null auto_increment,
    success bit not null,
    timestamp datetime not null,
    tx_id varchar(255) not null,
    version bigint not null,
    primary key (id)
) ENGINE=InnoDB;

create table report_time (
    id bigint not null auto_increment,
    time_in_ms bigint not null,
    update_event_handler_simple_class_name varchar(255) not null,
    report_execution_id bigint not null,
    primary key (id)
) ENGINE=InnoDB;

alter table report_count 
    add constraint FKs6tc2ldc9cruircvkn3yxf9xr 
    foreign key (report_execution_id) 
    references report_execution (id);

alter table report_time 
    add constraint FKjbywercga4huu3cxb2mml9k48 
    foreign key (report_execution_id) 
    references report_execution (id);
