package com.witcraft.dependencyscan;

import java.util.Map;

public interface PomScanner {
    Map<String, DependencyInfo> scanDependencyInfo();
}
