---
title: Linear Scalability
linkTitle: 1. Linear Scalability
description: Linear Scalability
menu:
  v1.1:
    identifier: pgsql-linear-scalability
    parent: explore-pgsql
    weight: 291
---

With Yugabyte DB, you can add nodes to scale your cluster up very efficiently and reliably in order to achieve more read and write IOPS. In this tutorial, we will look at how Yugabyte DB can scale while a workload is running. We will run a read-write workload using a pre-built PostgreSQL sample application against a 3-node local cluster with a replication factor of 3, and add nodes to it while the workload is running. We will then observe how the cluster scales out, by verifying that the number of read/write IOPS are evenly distributed across all the nodes at all times.

If you haven't installed Yugabyte DB yet, do so first by following the [Quick Start](../../../quick-start/install/) guide.

<ul class="nav nav-tabs nav-tabs-yb">
  <li >
    <a href="#macos" class="nav-link active" id="macos-tab" data-toggle="tab" role="tab" aria-controls="macos" aria-selected="true">
      <i class="fab fa-apple" aria-hidden="true"></i>
      macOS
    </a>
  </li>
  <li>
    <a href="#linux" class="nav-link" id="linux-tab" data-toggle="tab" role="tab" aria-controls="linux" aria-selected="v">
      <i class="fab fa-linux" aria-hidden="true"></i>
      Linux
    </a>
  </li>
  <!--
  <li>
    <a href="#docker" class="nav-link" id="docker-tab" data-toggle="tab" role="tab" aria-controls="docker" aria-selected="false">
      <i class="fab fa-docker" aria-hidden="true"></i>
      Docker
    </a>
  </li>
-->
</ul>

<div class="tab-content">
  <div id="macos" class="tab-pane fade show active" role="tabpanel" aria-labelledby="macos-tab">
    {{% includeMarkdown "binary/linear-scalability.md" /%}}
  </div>
  <div id="linux" class="tab-pane fade" role="tabpanel" aria-labelledby="linux-tab">
    {{% includeMarkdown "binary/linear-scalability.md" /%}}
  </div>
  <!--
  <div id="docker" class="tab-pane fade" role="tabpanel" aria-labelledby="docker-tab">
    {{% includeMarkdown "docker/linear-scalability.md" /%}}
  </div>
  -->
</div>
