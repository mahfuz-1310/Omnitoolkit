JAR=$(find / -name "jcifs-ng-2.1.10.jar" 2>/dev/null | head -n 1)
javap -cp $JAR jcifs.netbios.NbtAddress
