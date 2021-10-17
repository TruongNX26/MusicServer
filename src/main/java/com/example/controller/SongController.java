package com.example.controller;

import com.example.entity.Song;
import com.example.entity.SongModel;
import com.example.repository.SongRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/songs")
public class SongController {

    private String savePath = "D:\\File\\music\\";
    private SongRepository repository;

    public SongController(SongRepository repository) {
        this.repository = repository;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createMusic(@ModelAttribute SongModel model, @RequestPart MultipartFile data) throws IOException {

        if(data == null) throw new RuntimeException("Create Song failed");

        Song song = Song.builder()
                .name(model.getName())
                .name(model.getName())
                .singer(model.getSinger()).build();

        song = repository.save(song);

        saveFile(data, song.getId());

        return ResponseEntity.ok().body(song);
    }

    @GetMapping
    public ResponseEntity getAllSongs() throws FileNotFoundException {

        List<Song> songs = repository.findAll();
        List<SongModel> models = new ArrayList<>();

        SongModel model;
        for(Song song : songs) {
            model = SongModel.builder()
                    .id(song.getId())
                    .name(song.getName())
                    .singer(song.getSinger()).build();

            model.add(WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder
                            .methodOn(this.getClass())
                            .getData(song.getId()))
                    .withRel("data"));

            models.add(model);
        }

        return ResponseEntity.ok().body(models);
    }

    @GetMapping(
            value = "/{id}/data",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getData(@PathVariable Long id) throws FileNotFoundException {
        File file = new File(savePath + formatFileName(id));
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSong(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private String formatFileName(Long id) {
        return String.format("%d_music", id);
    }

    private void saveFile(MultipartFile multipartFile, Long id) throws IOException {
        byte[] data = multipartFile.getBytes();
        String filePath = savePath + formatFileName(id);
        FileOutputStream outputStream = new FileOutputStream(new File(filePath));
        outputStream.write(data);
        outputStream.close();
    }

}
