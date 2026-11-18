package gut.dev.veloxplay.Controller;

import gut.dev.veloxplay.Service.SFilme;
import gut.dev.veloxplay.dto.FilmeRequest;
import gut.dev.veloxplay.dto.FilmeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/filmes")
public class CFilme {
    private final SFilme serviceFilme;

    public CFilme(SFilme serviceFilme) {
        this.serviceFilme = serviceFilme;
    }

    @GetMapping
    public List<FilmeResponse> getAllFilmes() {
        return serviceFilme.findAll().stream().map(FilmeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public FilmeResponse getFilmeById(@PathVariable Long id) {
        return FilmeResponse.from(serviceFilme.findById(id));
    }

    @PostMapping
    public ResponseEntity<FilmeResponse> createFilme(@Valid @RequestBody FilmeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(FilmeResponse.from(serviceFilme.create(request)));
    }

    @PutMapping("/{id}")
    public FilmeResponse updateFilme(@PathVariable Long id, @Valid @RequestBody FilmeRequest request) {
        return FilmeResponse.from(serviceFilme.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilme(@PathVariable Long id) {
        serviceFilme.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
