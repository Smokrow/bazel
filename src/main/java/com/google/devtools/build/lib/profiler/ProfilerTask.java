// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.profiler;

import java.time.Duration;

/**
 * All possible types of profiler tasks. Each type also defines description and
 * minimum duration in nanoseconds for it to be recorded as separate event and
 * not just be aggregated into the parent event.
 */
public enum ProfilerTask {
  PHASE("build phase marker"),
  ACTION("action processing"),
  ACTION_CHECK("action dependency checking", Threshold.TEN_MILLIS),
  ACTION_LOCK("action resource lock", Threshold.TEN_MILLIS),
  ACTION_RELEASE("action resource release", Threshold.TEN_MILLIS),
  ACTION_UPDATE("update action information", Threshold.TEN_MILLIS),
  ACTION_COMPLETE("complete action execution"),
  INFO("general information"),
  CREATE_PACKAGE("package creation"),
  REMOTE_EXECUTION("remote action execution"),
  LOCAL_EXECUTION("local action execution"),
  SCANNER("include scanner"),
  // 30 is a good number because the slowest items are stored in a heap, with temporarily
  // one more element, and with 31 items, a heap becomes a complete binary tree
  LOCAL_PARSE("Local parse to prepare for remote execution", Threshold.FIFTY_MILLIS, 30),
  UPLOAD_TIME("Remote execution upload time", Threshold.FIFTY_MILLIS),
  PROCESS_TIME("Remote execution process wall time", Threshold.FIFTY_MILLIS),
  REMOTE_QUEUE("Remote execution queuing time", Threshold.FIFTY_MILLIS),
  REMOTE_SETUP("Remote execution setup", Threshold.FIFTY_MILLIS),
  FETCH("Remote execution file fetching", Threshold.FIFTY_MILLIS),
  VFS_STAT("VFS stat", Threshold.TEN_MILLIS, 30),
  VFS_DIR("VFS readdir", Threshold.TEN_MILLIS, 30),
  VFS_READLINK("VFS readlink", Threshold.TEN_MILLIS, 30),
  // TODO(olaola): rename to VFS_DIGEST. This refers to all digest function computations.
  VFS_MD5("VFS md5", Threshold.TEN_MILLIS, 30),
  VFS_XATTR("VFS xattr", Threshold.TEN_MILLIS, 30),
  VFS_DELETE("VFS delete", Threshold.TEN_MILLIS),
  VFS_OPEN("VFS open", Threshold.TEN_MILLIS, 30),
  VFS_READ("VFS read", Threshold.TEN_MILLIS, 30),
  VFS_WRITE("VFS write", Threshold.TEN_MILLIS, 30),
  VFS_GLOB("globbing", null, 30),
  VFS_VMFS_STAT("VMFS stat", Threshold.TEN_MILLIS),
  VFS_VMFS_DIR("VMFS readdir", Threshold.TEN_MILLIS),
  VFS_VMFS_READ("VMFS read", Threshold.TEN_MILLIS),
  WAIT("thread wait", Threshold.TEN_MILLIS),
  THREAD_NAME("thread name"), // Do not use directly!
  SKYFRAME_EVAL("skyframe evaluator"),
  SKYFUNCTION("skyfunction"),
  CRITICAL_PATH("critical path"),
  CRITICAL_PATH_COMPONENT("critical path component"),
  HANDLE_GC_NOTIFICATION("gc notification"),
  LOCAL_CPU_USAGE("cpu counters"),
  ACTION_COUNTS("action counters"),
  STARLARK_PARSER("Starlark Parser"),
  STARLARK_USER_FN("Starlark user function call"),
  STARLARK_BUILTIN_FN("Starlark builtin function call"),
  STARLARK_USER_COMPILED_FN("Starlark compiled user function call"),
  ACTION_FS_STAGING("Staging per-action file system"),
  REMOTE_CACHE_CHECK("remote action cache check"),
  REMOTE_DOWNLOAD("remote output download"),
  REMOTE_NETWORK("remote network"),
  UNKNOWN("Unknown event");

  private static class Threshold {
    private static final Duration TEN_MILLIS = Duration.ofMillis(10);
    private static final Duration FIFTY_MILLIS = Duration.ofMillis(50);
  }

  // Size of the ProfilerTask value space.
  public static final int TASK_COUNT = ProfilerTask.values().length;

  /** Human readable description for the task. */
  public final String description;
  /**
   * Threshold for skipping tasks in the profile in nanoseconds, unless --record_full_profiler_data
   * is used.
   */
  public final long minDuration;
  /** How many of the slowest instances to keep. If 0, no slowest instance calculation is done. */
  public final int slowestInstancesCount;
  /** True if the metric records VFS operations */
  private final boolean vfs;

  ProfilerTask(String description, Duration minDuration, int slowestInstanceCount) {
    this.description = description;
    this.minDuration = minDuration == null ? -1 : minDuration.toNanos();
    this.slowestInstancesCount = slowestInstanceCount;
    this.vfs = this.name().startsWith("VFS");
  }

  ProfilerTask(String description, Duration minDuration) {
    this(description, minDuration, /* slowestInstanceCount= */ 0);
  }

  ProfilerTask(String description) {
    this(description, /* minDuration= */ null, /* slowestInstanceCount= */ 0);
  }

  /** Whether the Profiler collects the slowest instances of this task. */
  public boolean collectsSlowestInstances() {
    return slowestInstancesCount > 0;
  }

  public boolean isVfs() {
    return vfs;
  }

  public boolean isStarlark() {
    return description.startsWith("Starlark ");
  }
}
