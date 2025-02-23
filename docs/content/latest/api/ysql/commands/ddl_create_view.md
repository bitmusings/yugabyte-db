---
title: CREATE VIEW
linkTitle: CREATE VIEW
summary: Create a new view in a database
description: CREATE VIEW
menu:
  latest:
    identifier: api-ysql-commands-create-view
    parent: api-ysql-commands
aliases:
  - /latest/api/ysql/ddl_create_view/
isTocNested: true
showAsideToc: true
---

## Synopsis

Use the `CREATE VIEW` statement to create a new view in a database. It defines the view name and the (select) statement defining it.  

## Syntax

<ul class="nav nav-tabs nav-tabs-yb">
  <li >
    <a href="#grammar" class="nav-link active" id="grammar-tab" data-toggle="tab" role="tab" aria-controls="grammar" aria-selected="true">
      <i class="fas fa-file-alt" aria-hidden="true"></i>
      Grammar
    </a>
  </li>
  <li>
    <a href="#diagram" class="nav-link" id="diagram-tab" data-toggle="tab" role="tab" aria-controls="diagram" aria-selected="false">
      <i class="fas fa-project-diagram" aria-hidden="true"></i>
      Diagram
    </a>
  </li>
</ul>

<div class="tab-content">
  <div id="grammar" class="tab-pane fade show active" role="tabpanel" aria-labelledby="grammar-tab">
    {{% includeMarkdown "../syntax_resources/commands/create_view.grammar.md" /%}}
  </div>
  <div id="diagram" class="tab-pane fade" role="tabpanel" aria-labelledby="diagram-tab">
    {{% includeMarkdown "../syntax_resources/commands/create_view.diagram.md" /%}}
  </div>
</div>

## Semantics

### *create_view*

#### CREATE [ OR REPLACE ] VIEW *qualified_name* [ (*column_list* ) ] AS select

Create a view.

##### *qualified_name*

Specify the name of the view. An error is raised if view with that name already exists in the specified database (unless the `OR REPLACE` option is used).

##### *column_list*

Specify a comma-separated list of columns. If not specified, the column names are deduced from the query.

###### *select*

Specify a `SELECT` or `VALUES` statement that will provide the columns and rows of the view.

## Examples

Create a sample table.

```sql
CREATE TABLE sample(k1 int, k2 int, v1 int, v2 text, PRIMARY KEY (k1, k2));
```

Insert some rows.

```sql
INSERT INTO sample(k1, k2, v1, v2) VALUES (1, 2.0, 3, 'a'), (2, 3.0, 4, 'b'), (3, 4.0, 5, 'c');
```

Create a view on the `sample` table.

```sql
CREATE VIEW sample_view AS SELECT * FROM sample WHERE v2 != 'b' ORDER BY k1 DESC;
```

Select from the view.

```sql
yugabyte=# SELECT * FROM sample_view;
```

```
 k1 | k2 | v1 | v2
----+----+----+----
  3 |  4 |  5 | c
  1 |  2 |  3 | a
(2 rows)
```

## See also

- [`DROP VIEW`](../ddl_drop_view)
- [`SELECT`](../dml_select)
