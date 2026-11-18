package gut.dev.veloxplay.Service;

import gut.dev.veloxplay.Model.Filme;
import gut.dev.veloxplay.Repository.RFilme;
import gut.dev.veloxplay.Repository.RUser;
import org.springframework.security.access.prepost.PreAuthorize;
import gut.dev.veloxplay.dto.FilmeRequest;
import gut.dev.veloxplay.exception.ConflictException;
import gut.dev.veloxplay.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SFilme {
    private final RFilme repository;
    private final RUser users;

    public SFilme(RFilme repository, RUser users) {
        this.repository = repository;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Filme> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Filme findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Filme create(FilmeRequest request) {
        if (repository.existsByTituloIgnoreCase(request.getTitulo())) {
            throw new ConflictException("Já existe um filme com este título");
        }
        return repository.save(apply(new Filme(), request));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Filme update(Long id, FilmeRequest request) {
        Filme existing = findById(id);
        repository.findByTituloIgnoreCase(request.getTitulo())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new ConflictException("Já existe um filme com este título"); });
        return repository.save(apply(existing, request));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id) {
        Filme existing = findById(id);
        users.findDistinctByFavoritosId(id).forEach(user -> user.getFavoritos().remove(existing));
        users.flush();
        repository.delete(existing);
    }

    private Filme apply(Filme filme, FilmeRequest request) {
        filme.setTitulo(request.getTitulo());
        filme.setDescricao(request.getDescricao());
        filme.setGenero(request.getGenero());
        filme.setUrlVideo(request.getUrlVideo());
        filme.setUrlCapa(request.getUrlCapa());
        String backdrop = request.getUrlBackdrop();
        filme.setUrlBackdrop(backdrop == null || backdrop.isBlank() ? null : backdrop.trim());
        filme.setAno(request.getAno());
        filme.setDuracaoMinutos(request.getDuracaoMinutos());
        filme.setClassificacao(request.getClassificacao());
        filme.setAvaliacao(request.getAvaliacao());
        filme.setDestaque(Boolean.TRUE.equals(request.getDestaque()));
        return filme;
    }
}
