package com.saeed.movie.moviessearch.service.data;



import com.saeed.movie.moviessearch.domain.*;
import com.saeed.movie.moviessearch.error.ProcessingException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;

@Log
@Component
public class DataLoader {

    private static final int BATCH_SIZE = 25;

    private final ThreadPoolExecutor workers;

    private final FileOperations fileOperations;

    private List<String> unzippedFiles;

    // k ---> zippedFileName , v ---> unzippedFileName
    private Map<String, String> zippedFilesBindings;

    @Value("${clean.unzipped.files}")
    private boolean cleanUnzippedFiles;

    @Value("${output.directory}")
    private String outputFileDir;

    private final DataInjector dataInjector;

    private final DataLoaderMetrics dataLoaderMetrics;

    @Autowired
    public DataLoader(FileOperations fileOperations, ThreadPoolExecutor workers, DataInjector dataInjector, DataLoaderMetrics dataLoaderMetrics) {
        this.fileOperations = fileOperations;
        this.workers = workers;
        this.dataInjector = dataInjector;
        this.dataLoaderMetrics = dataLoaderMetrics;
    }

    public void runEtlJob() {
        initZippedFilesBindings();
        if (!allUnzippedFilesExist()) {
            long startTime = System.nanoTime();
           unzipFiles();
            long totalTime = System.nanoTime() - startTime;

            log.info("Total time to extract in ms: " + TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS));
            log.info("Unzipped-filenames: " + unzippedFiles);
        }
        long heapSize = Runtime.getRuntime().totalMemory();
        log.info("heap size from saeed:"+heapSize);
        long startTime = System.nanoTime();
        loadData();
        long totalTime = System.nanoTime() - startTime;
        log.info("Total time to load data in ms: " + TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS));

        dataLoaderMetrics.addSuccessfulRun();
    }

    @PreDestroy
    void clear() {
        if (!cleanUnzippedFiles) {
            return;
        }

        unzippedFiles.forEach(unzippedFile -> {
            try {
                Files.deleteIfExists(Paths.get(unzippedFile));
            } catch (IOException e) {
                log.log(SEVERE, e.getMessage());
            }
        });
    }

    private void initZippedFilesBindings() {
        this.zippedFilesBindings = new HashMap<String, String>() {
            {
                String fileSeparator = File.separator;
                put("data/name.basics.tsv.gz", outputFileDir +fileSeparator+ "name.basics.tsv");
                put("data/title.akas.tsv.gz", outputFileDir +fileSeparator+ "title.akas.tsv");
                put("data/title.basics.tsv.gz", outputFileDir +fileSeparator+ "title.basics.tsv");
                put("data/title.crew.tsv.gz", outputFileDir +fileSeparator+ "title.crew.tsv");
                put("data/title.episode.tsv.gz", outputFileDir +fileSeparator+ "title.episode.tsv");
                put("data/title.principals.tsv.gz", outputFileDir +fileSeparator+ "title.principals.tsv");
                put("data/title.ratings.tsv.gz", outputFileDir +fileSeparator+ "title.ratings.tsv");
            }
        };
    }

    private Boolean allUnzippedFilesExist() {
        return zippedFilesBindings.values().stream()
                .map(unzippedFileName -> Files.exists(Paths.get(unzippedFileName)))
                .reduce(true, (acc, elem) -> acc && elem);
    }

    private void unzipFiles() {
        final List<Future<String>> unzippedFilesResult = zippedFilesBindings
                .entrySet()
                .stream()
                .map(entry -> (Callable<String>) () -> fileOperations.unzip(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())
                .stream()
                .map(workers::submit)
                .collect(Collectors.toList());

        unzippedFiles = unzippedFilesResult
                .stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception error) {
                        log.log(Level.SEVERE, error.getMessage());
                        throw new ProcessingException("DataLoader#unzipFiles, error: " + error.getMessage(), error);
                    }
                })
                .collect(Collectors.toList());
    }

    private void loadData()  {
    /*  List<Name> names = batchStore(outputFileDir + "/name.basics.tsv", Name::new);
        List<Crew> crews = batchStore(outputFileDir + "/title.crew.tsv", Crew::new);*/

     final List<Future<?>> loadDataResults = Stream
                .<Runnable>of(

              /*          () -> batchStore(outputFileDir + "/title.ratings.tsv", Rating::new),
                        () -> batchStore(outputFileDir + "/title.principals.tsv", Principal::new),
                        () -> batchStore(outputFileDir + "/title.episode.tsv", Episode::new),*/
                       () -> batchStore(outputFileDir + "/name.basics.tsv", Name::new)/* ,
                        () -> batchStore(outputFileDir + "/title.crew.tsv", Crew::new)
                      () -> batchStore(outputFileDir + "/title.basics.tsv", Basic::new),
                        () -> batchStore(outputFileDir + "/title.akas.tsv", Aka::new)*/
                )
                .map(workers::submit)
                .collect(Collectors.toList());

        loadDataResults.forEach(f -> {
            try {
                f.get();
            } catch (Exception error) {
                log.log(Level.SEVERE, error.getMessage());
                throw new ProcessingException("DataLoader#loadData, error: " + error.getMessage(), error);
            }
        });

    }

    private <T> List<T> batchStore( String unzippedFileName, Function<List<String>, T> transformation) {

        final List<T> batchedEntities = new ArrayList<>(BATCH_SIZE);
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(unzippedFileName))) {

            String line;
            bufferedReader.readLine(); // skip header line
            ArrayList<String> nconstOfSameWritersAndDirectories = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {

                final List<String> data = Arrays.asList(line.split("\t"));

                T entity = transformation.apply(data);

                batchedEntities.add(entity);
    /*            List<String> directories = Collections.unmodifiableList(Arrays.asList(data.get(1).split(",")));
                List<String> writers = Collections.unmodifiableList(Arrays.asList(data.get(2).split(",")));
                List<String> collect = directories.stream()
                        .filter(writers::contains)
                        .collect(Collectors.toList());
                if(collect.size()>0){
                    nconstOfSameWritersAndDirectories.add(data.get(0));
                }*/
                if (batchedEntities.size() % BATCH_SIZE == 0) {

                   // log.info("currently processing data from file: " + unzippedFileName);

                  //  dataInjector.perform(batchedEntities);

                }
            }

            return (List<T>) nconstOfSameWritersAndDirectories;

        } catch (IOException error) {
            log.log(Level.SEVERE, error.getMessage());
            throw new ProcessingException("DataLoader#batchStore, error: " + error.getMessage(), error);
        }
    }
}
