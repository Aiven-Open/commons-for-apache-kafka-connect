/*
 * Copyright 2023 Aiven Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


plugins {
    id("aiven-apache-kafka-connectors-all.java-conventions")
}

val kafkaVersion by extra ("1.1.0")
val parquetVersion by extra ("1.11.2")
val junitVersion by extra ("5.10.2")
val confluentPlatformVersion by extra ("7.2.2")
val hadoopVersion by extra ("3.3.6")

dependencies {
    compileOnly("org.apache.kafka:connect-api:$kafkaVersion")
    compileOnly("org.apache.kafka:connect-runtime:$kafkaVersion")
    compileOnly("org.apache.kafka:connect-json:$kafkaVersion")

    implementation("io.confluent:kafka-connect-avro-data:$confluentPlatformVersion") {
        exclude(group = "org.apache.kafka", module = "kafka-clients")
    }

    implementation("com.github.spotbugs:spotbugs-annotations:4.8.1")
    implementation("org.xerial.snappy:snappy-java:1.1.10.5")
    implementation("com.github.luben:zstd-jni:1.5.5-10")

    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation("org.apache.commons:commons-text:1.11.0")

    implementation("org.apache.parquet:parquet-avro:$parquetVersion") {
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.apache.avro", module = "avro")
    }
    implementation("org.apache.hadoop:hadoop-common:$hadoopVersion") {
        exclude(group = "org.apache.hadoop.thirdparty", module = "hadoop-shaded-protobuf_3_7")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "commons-cli", module = "commons-cli")
        exclude(group = "org.apache.commons", module = "commons-math3")
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "commons-net", module = "commons-net")
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "javax.servlet")
        exclude(group = "javax.servlet.jsp")
        exclude(group = "javax.activation")
        exclude(group = "com.sun.jersey")
        exclude(group = "log4j")
        exclude(group = "org.apache.commons", module = "commons-text")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.apache.hadoop", module = "hadoop-auth")
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-api")
        exclude(group = "com.google.re2j")
        exclude(group = "com.google.protobuf")
        exclude(group = "com.google.code.gson")
        exclude(group = "com.jcraft")
        exclude(group = "org.apache.curator")
        exclude(group = "org.apache.zookeeper")
        exclude(group = "org.apache.htrace")
        exclude(group = "com.google.code.findbugs")
        exclude(group = "org.apache.kerby")
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.woodstox", module = "woodstox-core:5.0.3")
        exclude(group = "org.apache.avro", module = "avro")
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-common")
        exclude(group = "com.google.inject.extensions", module = "guice-servlet")
        exclude(group = "io.netty", module = "netty")
    }

    testImplementation("org.apache.kafka:connect-api:$kafkaVersion")
    testImplementation("org.apache.kafka:connect-runtime:$kafkaVersion")
    testImplementation("org.apache.kafka:connect-json:$kafkaVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.apache.parquet:parquet-tools:$parquetVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.assertj:assertj-core:3.24.2")

    testImplementation("org.apache.parquet:parquet-tools:$parquetVersion") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testImplementation("org.codehaus.woodstox:stax2-api:4.2.2")
    testImplementation("org.apache.hadoop:hadoop-mapreduce-client-core:$hadoopVersion")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    testImplementation("io.confluent:kafka-connect-avro-converter:$confluentPlatformVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.11")
}

distributions {
    main {
        contents {
            from("jar")
            from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })


            into("/") {
                from("$projectDir")
                include("version.txt", "README*", "LICENSE*", "NOTICE*", "licenses/")
                include("config/")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("publishMavenJavaArtifact") {
            groupId = group.toString()
            artifactId = "commons-for-apache-kafka-connect"
            version = version.toString()

            from(components["java"])

            pom {
                name = "Aiven's Common Module for Apache Kafka connectors"
                description = "Aiven's Common Module for Apache Kafka connectors"
                url = "https://github.com/aiven-open/commons-for-apache-kafka-connect"
                organization {
                    name = "Aiven Oy"
                    url = "https://aiven.io"
                }

                licenses {
                    license {
                        name = "Apache 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                        distribution = "repo"
                    }
                }

                developers {
                    developer {
                        id = "aiven"
                        name = "Aiven Opensource"
                        email = "opensource@aiven.io"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com:aiven-open/commons-for-apache-kafka-connect.git"
                    developerConnection = "scm:git:ssh://github.com:aiven-open/commons-for-apache-kafka-connect.git"
                    url = "https://github.com/aiven-open/commons-for-apache-kafka-connect"
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"

            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    sign(publishing.publications["publishMavenJavaArtifact"])
    useGpgCmd()
    // Some issue in the plugin:
    // GPG outputs already armored signatures. The plugin also does armoring for `asc` files.
    // This results in double armored signatures, i.e. garbage.
    // Override the signature type provider to use unarmored output for `asc` files, which works well with GPG.
    class ASCSignatureProvider() : AbstractSignatureTypeProvider() {
        val binary = object: BinarySignatureType() {
            override fun getExtension(): String {
                return "asc";
            }
        }
        init {
            register(binary)
            setDefaultType(binary.extension)
        }
    }
    signatureTypes = ASCSignatureProvider()
}
