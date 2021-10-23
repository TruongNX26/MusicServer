package com.example.controller;

import com.example.entity.Song;
import com.example.repository.SongRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
    public ResponseEntity<Song> createNewMusic(@ModelAttribute Song entity) throws IOException {

        if(entity.getData() == null)
            throw new RuntimeException("Creating Song failed");

        Song song = Song.builder()
                .name(entity.getName())
                .singer(entity.getSinger()).build();

        song = repository.save(song);

        saveFile(entity.getData(), song.getId());

        return ResponseEntity.ok().body(song);
    }

    @GetMapping
    public ResponseEntity getAllSongs() throws FileNotFoundException {

        List<Song> songs = repository.findAll();

        return ResponseEntity.ok().body(songs);
    }

    @GetMapping(
            value = "/{id}/data",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getDataOfSongHasId(@PathVariable Long id) throws FileNotFoundException {

        File file = new File(savePath + formatFileName(id));
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSongHasId(@PathVariable Long id) {
        System.out.println(id);

        repository.deleteById(id);

        File file = new File(String.format("%s%d_music", savePath, id));

        if(file.exists())
            file.delete();

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
        outputStream.flush();
        outputStream.close();
    }

}
