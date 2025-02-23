---
title: Backing Up Data
linkTitle: Backing Up Data
description: Backing Up Data
image: /images/section_icons/manage/enterprise.png
headcontent: Backing up data in Yugabyte DB.
menu:
  v1.2:
    identifier: manage-backup-restore-backing-up-data
    parent: manage-backup-restore
    weight: 703
---

This page covers backups for Yugabyte DB CE. Here are some points to keep in mind.

- Export-based backups (Yugabyte DB CE)
  - Single row ACID backups
  - Backup the schema and data separately
  - Multi-threaded parallelism

- Distributed backups (Yugabyte DB EE)
  - Single table ACID backups
  - Integrated backup solution, integrated with object stores such as AWS S3
  - Massively parallel, efficient for very large data sets

Dump the keyspace schema (optional) as well as the data in the tables to create a backup of the data in Yugabyte DB. Here are some points to bear in mind.

<ul class="nav nav-tabs nav-tabs-yb">
  <li>
    <a href="#cassandra" class="nav-link active" id="cassandra-tab" data-toggle="tab" role="tab" aria-controls="cassandra" aria-selected="true">
      <i class="icon-cassandra" aria-hidden="true"></i>
      YCQL
    </a>
  </li>
</ul>

<div class="tab-content">
  <div id="cassandra" class="tab-pane fade show active" role="tabpanel" aria-labelledby="cassandra-tab">
    {{% includeMarkdown "ycql/backing-up-data.md" /%}}
  </div>
</div>




