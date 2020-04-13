create table alert_to_send (
    id bigint not null auto_increment,
    content varchar(10000),
    sender varchar(255),
    sent_on datetime,
    subject varchar(255),
    primary key (id)
) ENGINE=InnoDB;
