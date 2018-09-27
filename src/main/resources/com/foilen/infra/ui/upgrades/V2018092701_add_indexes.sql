create index if not exists audit_item_type_id on audit_item (type, id desc);

create index if not exists plugin_resource_type on plugin_resource (type);

create index if not exists plugin_resource_column_search_column_name on plugin_resource_column_search (column_name);

create index if not exists plugin_resource_link_from_type on plugin_resource_link (from_plugin_resource_id, link_type);

create index if not exists plugin_resource_link_to_type on plugin_resource_link (to_plugin_resource_id, link_type);
