import com.github.spotbugs.snom.SpotBugsTask

/*
 * Copyright 2020 Aiven Oy
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

group = "io.aiven"

// TODO: document why we stick to these versions
val kafkaVersion by extra ("1.1.0")
val confluentPlatformVersion by extra ("4.1.4")
val avroConverterVersion by extra ("7.2.2")
val slf4jVersion by extra ("1.7.36")
val testcontainersVersion by extra("1.19.8")
val wireMockVersion by extra("2.35.0")
val mockitoVersion by extra("5.2.0")

val integrationTest: SourceSet = sourceSets.create("integrationTest") {
    java {
        srcDir("src/integration-test/java")
    }
    resources {
        srcDir("src/integration-test/resources")
    }
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath

    // defines testing order
    shouldRunAfter("test")
    // requires archive for connect runner
    dependsOn("distTar")
    useJUnitPlatform()

    // Run always.
    outputs.upToDateWhen { false }

    // Pass the GCS credentials path to the tests.
    if (project.hasProperty("gcsCredentialsPath")) {
        systemProperty("integration-test.gcs.credentials.path", project.findProperty("gcsCredentialsPath").toString())
    }
    // Pass the GCS credentials JSON to the tests.
    if (project.hasProperty("gcsCredentialsJson")) {
        systemProperty("integration-test.gcs.credentials.json", project.findProperty("gcsCredentialsJson").toString())
    }
    // Pass the GCS bucket name to the tests.
    systemProperty("integration-test.gcs.bucket", project.findProperty("testGcsBucket").toString())
    // Pass the distribution file path to the tests.
    val distTarTask = tasks.get("distTar") as Tar
    val distributionFilePath = distTarTask.archiveFile.get().asFile.path
    systemProperty("integration-test.distribution.file.path", distributionFilePath)
    systemProperty("fake-gcs-server-version", "1.45.2")
}

idea {
    module {
        testSources.from(integrationTest.java.srcDirs)
        testSources.from(integrationTest.resources.srcDirs)
    }
}

dependencies {
    compileOnly("org.apache.kafka:connect-api:$kafkaVersion")
    compileOnly("org.apache.kafka:connect-runtime:$kafkaVersion")

    implementation(project(":commons"))

    implementation("com.google.cloud:google-cloud-storage:2.37.0") {
        exclude(group = "com.google.guava", module = "guava")
    }
    // TODO: document why specific version of guava is required
    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("com.github.spotbugs:spotbugs-annotations:4.8.4")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("net.jqwik:jqwik:1.8.4")
    // is provided by "jqwik", but need this in testImplementation scope
    testImplementation("net.jqwik:jqwik-engine:1.8.3")

    testImplementation("org.apache.kafka:connect-api:$kafkaVersion")
    testImplementation("org.apache.kafka:connect-runtime:$kafkaVersion")
    testImplementation("org.apache.kafka:connect-json:$kafkaVersion")
    testImplementation("com.google.cloud:google-cloud-nio:0.127.16")

    testImplementation("org.xerial.snappy:snappy-java:1.1.10.5")
    testImplementation("com.github.luben:zstd-jni:1.5.6-1")
    testImplementation ("org.apache.parquet:parquet-tools:1.11.2") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testImplementation("org.apache.hadoop:hadoop-mapreduce-client-core:3.3.6") {
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-client")
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

    testRuntimeOnly("org.slf4j:slf4j-log4j12:$slf4jVersion")

    integrationTestImplementation("com.github.tomakehurst:wiremock-jre8:$wireMockVersion")
    integrationTestImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    integrationTestImplementation("org.testcontainers:kafka:$testcontainersVersion") // this is not Kafka version
    integrationTestImplementation("org.awaitility:awaitility:4.2.0")

    integrationTestImplementation("org.apache.kafka:connect-transforms:$kafkaVersion")
    // TODO: add avro-converter to ConnectRunner via plugin.path instead of on worker classpath
    integrationTestImplementation("io.confluent:kafka-connect-avro-converter:$avroConverterVersion") {
        exclude(group = "org.apache.kafka", module = "kafka-clients")
    }

    // Make test utils from "test" available in "integration-test"
    integrationTestImplementation(sourceSets["test"].output)
}

tasks.named<Pmd>("pmdIntegrationTest") {
    ruleSetFiles = files("${project.rootDir}/gradle-config/aiven-pmd-test-ruleset.xml")
    ruleSets = emptyList() // Clear the default rulesets
}

tasks.named<SpotBugsTask>("spotbugsIntegrationTest") {
    reports.create("html") {
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.processResources {
    filesMatching("gcs-connector-for-apache-kafka-version.properties") {
        expand(mapOf("version" to version))
    }
}

tasks.jar {
    manifest {
        attributes(mapOf("Version" to project.version))
    }
}

tasks.distTar {
    dependsOn(":commons:jar")
}
tasks.distZip {
    dependsOn(":commons:jar")
}

distributions {
    main {
        contents {
            from("jar")
            from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("publishMavenJavaArtifact") {
            groupId = group.toString()
            artifactId = "gcs-connector-for-apache-kafka"
            version = version.toString()

            from(components["java"])

            pom {
                name = "Aiven's GCS Sink Connector for Apache Kafka"
                description = "Aiven's GCS Sink Connector for Apache Kafka"
                url = "https://github.com/aiven/gcs-connector-for-apache-kafka"
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
                    connection = "scm:git:git://github.com:aiven/gcs-connector-for-apache-kafka.git"
                    developerConnection = "scm:git:ssh://github.com:aiven/gcs-connector-for-apache-kafka.git"
                    url = "https://github.com/aiven/gcs-connector-for-apache-kafka"
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