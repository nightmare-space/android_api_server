./gradlew :aas:assembleRelease
mvn install:install-file \
  -Dfile=aas/build/outputs/aar/aas-release.aar \
  -DgroupId=com.github.nightmare-space.android_api_server \
  -DartifactId=aas \
  -Dversion=1.0.0 \
  -Dpackaging=aar
./gradlew :aas_plugins:assembleRelease
mvn install:install-file \
  -Dfile=aas_plugins/build/outputs/aar/aas_plugins-release.aar \
  -DgroupId=com.github.nightmare-space.android_api_server \
  -DartifactId=aas_plugins \
  -Dversion=1.0.0 \
  -Dpackaging=aar
./gradlew :aas_hidden_api:assembleRelease
mvn install:install-file \
  -Dfile=aas_hidden_api/build/outputs/aar/aas_hidden_api-release.aar \
  -DgroupId=com.github.nightmare-space.android_api_server \
  -DartifactId=aas_hidden_api \
  -Dversion=1.0.0 \
  -Dpackaging=aar
./gradlew :aas_integrated:assembleRelease
mvn install:install-file \
  -Dfile=aas_integrated/build/outputs/aar/aas_integrated-release.aar \
  -DgroupId=com.github.nightmare-space.android_api_server \
  -DartifactId=aas_integrated \
  -Dversion=1.0.0 \
  -Dpackaging=aar