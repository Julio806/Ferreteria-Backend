package ferreteria_backend.Service.impl;

import ferreteria_backend.Entity.Categoria;
import ferreteria_backend.Exception.ResourceNotFoundException;
import ferreteria_backend.Repository.CategoriaRepository;
import ferreteria_backend.Service.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(
            CategoriaRepository categoriaRepository
    ) {
        this.categoriaRepository = categoriaRepository;
    }

    // ==========================================
    // CREAR CATEGORÍA
    // ==========================================

    @Override
    public Categoria crear(Categoria categoria) {

        if (categoriaRepository.existsByNombreIgnoreCase(
                categoria.getNombre()
        )) {

            throw new RuntimeException(
                    "Ya existe una categoría con el nombre: "
                            + categoria.getNombre()
            );
        }

        categoria.setActivo(true);

        return categoriaRepository.save(categoria);
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Override
    public Categoria obtenerPorId(Long id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró la categoría con ID: "
                                        + id
                        )
                );
    }

    // ==========================================
    // OBTENER TODAS
    // ==========================================

    @Override
    public List<Categoria> obtenerTodas() {

        return categoriaRepository.findAll();
    }

    // ==========================================
    // OBTENER ACTIVAS
    // ==========================================

    @Override
    public List<Categoria> obtenerActivas() {

        return categoriaRepository.findByActivoTrue();
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @Override
    public Categoria actualizar(
            Long id,
            Categoria categoria
    ) {

        Categoria categoriaExistente =
                obtenerPorId(id);

        if (!categoriaExistente.getNombre()
                .equalsIgnoreCase(categoria.getNombre())
                && categoriaRepository.existsByNombreIgnoreCase(
                categoria.getNombre()
        )) {

            throw new RuntimeException(
                    "Ya existe una categoría con el nombre: "
                            + categoria.getNombre()
            );
        }

        categoriaExistente.setNombre(
                categoria.getNombre()
        );

        categoriaExistente.setDescripcion(
                categoria.getDescripcion()
        );

        if (categoria.getActivo() != null) {

            categoriaExistente.setActivo(
                    categoria.getActivo()
            );
        }

        return categoriaRepository.save(
                categoriaExistente
        );
    }

    // ==========================================
    // ELIMINAR / DESACTIVAR
    // ==========================================

    @Override
    public void eliminar(Long id) {

        Categoria categoria =
                obtenerPorId(id);

        categoria.setActivo(false);

        categoriaRepository.save(categoria);
    }
}