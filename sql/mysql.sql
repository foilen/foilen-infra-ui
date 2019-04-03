
    create table alert_to_send (
        id bigint not null auto_increment,
        content varchar(10000),
        sender varchar(255),
        sent_on datetime,
        subject varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table api_machine_user (
        machine_name varchar(255) not null,
        user_key varchar(250) not null,
        id bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table api_user (
        id bigint not null auto_increment,
        is_admin bit not null,
        created_on datetime,
        description varchar(255) not null,
        expire_on datetime,
        user_hashed_key varchar(250) not null,
        user_id varchar(25) not null,
        version bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table audit_item (
        id bigint not null auto_increment,
        action varchar(50) not null,
        explicit_change bit not null,
        link_type varchar(255),
        resource_first longtext,
        resource_first_type varchar(255),
        resource_second longtext,
        resource_second_type varchar(255),
        tag_name varchar(255),
        timestamp datetime not null,
        tx_id varchar(255) not null,
        type varchar(50) not null,
        user_name varchar(255),
        user_type varchar(255) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table machine_statisticfs (
        id bigint not null auto_increment,
        is_root bit not null,
        path varchar(2000),
        total_space bigint not null,
        used_space bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table machine_statistic_network (
        id bigint not null auto_increment,
        in_bytes bigint not null,
        interface_name varchar(255),
        out_bytes bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table machine_statistics (
        id bigint not null auto_increment,
        aggregations_for_day integer not null,
        aggregations_for_hour integer not null,
        cpu_total bigint not null,
        cpu_used bigint not null,
        machine_internal_id bigint not null,
        memory_swap_total bigint not null,
        memory_swap_used bigint not null,
        memory_total bigint not null,
        memory_used bigint not null,
        timestamp datetime,
        primary key (id)
    ) ENGINE=InnoDB;

    create table machine_statistics_fs (
        machine_statistics_id bigint not null,
        fs_id bigint not null,
        primary key (machine_statistics_id, fs_id)
    ) ENGINE=InnoDB;

    create table machine_statistics_networks (
        machine_statistics_id bigint not null,
        networks_id bigint not null,
        primary key (machine_statistics_id, networks_id)
    ) ENGINE=InnoDB;

    create table plugin_resource (
        id bigint not null auto_increment,
        editor_name varchar(255),
        type varchar(255) not null,
        value_json longtext not null,
        version bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table plugin_resource_column_search (
        id bigint not null auto_increment,
        bool bit,
        column_name varchar(255) not null,
        double_number double precision,
        float_number float,
        int_number integer,
        long_number bigint,
        text varchar(4000),
        version bigint not null,
        plugin_resource_id bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table plugin_resource_link (
        id bigint not null auto_increment,
        link_type varchar(255) not null,
        version bigint not null,
        from_plugin_resource_id bigint not null,
        to_plugin_resource_id bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table plugin_resource_tag (
        id bigint not null auto_increment,
        tag_name varchar(255) not null,
        version bigint not null,
        plugin_resource_id bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

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

    create table user (
        id bigint not null auto_increment,
        is_admin bit not null,
        user_id varchar(255) not null,
        version bigint not null,
        primary key (id)
    ) ENGINE=InnoDB;

    alter table api_user 
        add constraint UK_6amqsmb5hmim0r67uoums68m4 unique (user_id);

    alter table machine_statistics_fs 
        add constraint UK_9dmogrrotqudkwogn3jxow1o7 unique (fs_id);

    alter table machine_statistics_networks 
        add constraint UK_jmuvbuf1gkt6xygfmfvt2r4f unique (networks_id);

    alter table plugin_resource_tag 
        add constraint UK6v7gutams8avyc1o051o06hbi unique (tag_name, plugin_resource_id);

    alter table user 
        add constraint UK_a3imlf41l37utmxiquukk8ajc unique (user_id);

    alter table api_machine_user 
        add constraint FKiiki0tm30l7gpwqp76oxpmj8g 
        foreign key (id) 
        references api_user (id);

    alter table machine_statistics_fs 
        add constraint FKs8ufpfbcnioxn4ps7cgkqma81 
        foreign key (fs_id) 
        references machine_statisticfs (id);

    alter table machine_statistics_fs 
        add constraint FK8bov3os0rkykiujxpwc2s9lqd 
        foreign key (machine_statistics_id) 
        references machine_statistics (id);

    alter table machine_statistics_networks 
        add constraint FK5pvvn1w3in92q14tyxvhi1lf4 
        foreign key (networks_id) 
        references machine_statistic_network (id);

    alter table machine_statistics_networks 
        add constraint FK28t7df9et3q9yadbluum71a4k 
        foreign key (machine_statistics_id) 
        references machine_statistics (id);

    alter table plugin_resource_column_search 
        add constraint FKkql9ueqcx65ow93p9c4myjlah 
        foreign key (plugin_resource_id) 
        references plugin_resource (id);

    alter table plugin_resource_link 
        add constraint FK1hwtaetkv8pab1q2dy7blbie 
        foreign key (from_plugin_resource_id) 
        references plugin_resource (id);

    alter table plugin_resource_link 
        add constraint FK5p0x37q0n9ojcqciy7d8tgwe1 
        foreign key (to_plugin_resource_id) 
        references plugin_resource (id);

    alter table plugin_resource_tag 
        add constraint FK7wjawogtvod7o8i9k26xlsw1o 
        foreign key (plugin_resource_id) 
        references plugin_resource (id);

    alter table report_count 
        add constraint FKs6tc2ldc9cruircvkn3yxf9xr 
        foreign key (report_execution_id) 
        references report_execution (id);

    alter table report_time 
        add constraint FKjbywercga4huu3cxb2mml9k48 
        foreign key (report_execution_id) 
        references report_execution (id);
