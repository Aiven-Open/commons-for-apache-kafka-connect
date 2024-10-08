/*
 * Copyright 2024 Aiven Oy
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

package io.aiven.kafka.connect.azure.sink;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.io.DelegatingSeekableInputStream;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

final class ParquetUtils {

    private ParquetUtils() {
        /* hide constructor */ }

    static List<GenericRecord> readRecords(final Path tmpDir, final byte[] bytes) throws IOException {
        final var records = new ArrayList<GenericRecord>();
        final var parquetFile = tmpDir.resolve("parquet.file");
        FileUtils.writeByteArrayToFile(parquetFile.toFile(), bytes);
        final var seekableByteChannel = Files.newByteChannel(parquetFile);
        try (var parquetReader = AvroParquetReader.<GenericRecord>builder(new InputFile() {
            @Override
            public long getLength() throws IOException {
                return seekableByteChannel.size();
            }

            @Override
            public SeekableInputStream newStream() {
                return new DelegatingSeekableInputStream(Channels.newInputStream(seekableByteChannel)) {
                    @Override
                    public long getPos() throws IOException {
                        return seekableByteChannel.position();
                    }

                    @Override
                    public void seek(final long value) throws IOException {
                        seekableByteChannel.position(value);
                    }
                };
            }

        }).withCompatibility(false).build()) {
            var record = parquetReader.read();
            while (record != null) {
                records.add(record);
                record = parquetReader.read();
            }
        }
        return records;
    }

}
