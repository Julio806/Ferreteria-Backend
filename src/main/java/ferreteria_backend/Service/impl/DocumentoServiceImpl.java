
package ferreteria_backend.Service.impl;

import ferreteria_backend.Entity.Documento;
import ferreteria_backend.Entity.Documento.TipoDocumento;
import ferreteria_backend.Exception.ResourceNotFoundException;
import ferreteria_backend.Repository.DocumentoRepository;
import ferreteria_backend.Service.DocumentoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;

    @Value("${app.uploads.plantillas:uploads/plantillas}")
    private String directorioPlantillas;

    public DocumentoServiceImpl(
            DocumentoRepository documentoRepository
    ) {
        this.documentoRepository = documentoRepository;
    }

    // ==========================================
    // SUBIR O REEMPLAZAR PLANTILLA
    // ==========================================

    @Override
    @Transactional
    public Documento subirPlantilla(
            TipoDocumento tipo,
            MultipartFile archivo
    ) {

        if (tipo == null) {
            throw new RuntimeException(
                    "Debes seleccionar el tipo de plantilla."
            );
        }

        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException(
                    "Debes seleccionar un archivo."
            );
        }

        String nombreOriginal = archivo.getOriginalFilename();

        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new RuntimeException(
                    "El archivo no tiene un nombre válido."
            );
        }

        String extension = obtenerExtension(nombreOriginal);

        validarExtension(tipo, extension);

        try {

            Path directorio = Paths.get(directorioPlantillas)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(directorio);

            String nombreNuevo =
                    UUID.randomUUID() + "." + extension;

            Path destino = directorio
                    .resolve(nombreNuevo)
                    .normalize();

            if (!destino.startsWith(directorio)) {
                throw new RuntimeException(
                        "Ruta de archivo no válida."
                );
            }

            // Guardar archivo nuevo
            archivo.transferTo(destino);

            // Buscar plantilla anterior
            Documento documento = documentoRepository
                    .findByTipo(tipo)
                    .orElse(new Documento());

            String rutaAnterior = documento.getRutaArchivo();

            documento.setNombre(obtenerNombrePlantilla(tipo));
            documento.setTipo(tipo);
            documento.setNombreArchivo(nombreOriginal);
            documento.setRutaArchivo(destino.toString());
            documento.setExtension(extension);

            // Guardar registro en PostgreSQL
            Documento guardado =
                    documentoRepository.saveAndFlush(documento);

            // Eliminar archivo anterior después de guardar el nuevo
            if (rutaAnterior != null && !rutaAnterior.isBlank()) {
                eliminarArchivoFisico(rutaAnterior);
            }

            return guardado;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo guardar la plantilla: " +
                            e.getMessage(),
                    e
            );

        }

    }

    // ==========================================
    // OBTENER TODAS LAS PLANTILLAS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Documento> obtenerTodos() {

        return documentoRepository.findAllByOrderByTipoAsc();

    }

    // ==========================================
    // OBTENER POR TIPO
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Documento obtenerPorTipo(TipoDocumento tipo) {

        return documentoRepository.findByTipo(tipo)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe una plantilla registrada para: "
                                        + tipo
                        )
                );

    }

    // ==========================================
    // ELIMINAR PLANTILLA
    // ==========================================

    @Override
    @Transactional
    public void eliminar(TipoDocumento tipo) {

        Documento documento = obtenerPorTipo(tipo);

        String ruta = documento.getRutaArchivo();

        documentoRepository.delete(documento);

        eliminarArchivoFisico(ruta);

    }

    // ==========================================
    // VALIDAR EXTENSIÓN
    // ==========================================

    private void validarExtension(
            TipoDocumento tipo,
            String extension
    ) {

        String extensionEsperada;

        switch (tipo) {

            case VENTAS_WORD, INVENTARIO_WORD:
                extensionEsperada = "docx";
                break;

            case VENTAS_EXCEL, INVENTARIO_EXCEL:
                extensionEsperada = "xlsx";
                break;

            default:
                throw new RuntimeException(
                        "Tipo de plantilla no permitido."
                );

        }

        if (!extension.equalsIgnoreCase(extensionEsperada)) {

            throw new RuntimeException(
                    "Esta plantilla requiere un archivo ." +
                            extensionEsperada
            );

        }

    }

    // ==========================================
    // OBTENER EXTENSIÓN
    // ==========================================

    private String obtenerExtension(String nombreArchivo) {

        int posicion = nombreArchivo.lastIndexOf('.');

        if (posicion < 0 || posicion == nombreArchivo.length() - 1) {

            throw new RuntimeException(
                    "El archivo debe tener una extensión válida."
            );

        }

        return nombreArchivo
                .substring(posicion + 1)
                .toLowerCase();

    }

    // ==========================================
    // NOMBRE DE PLANTILLA
    // ==========================================

    private String obtenerNombrePlantilla(TipoDocumento tipo) {

        return switch (tipo) {

            case VENTAS_WORD ->
                    "Hoja membretada - Word";

            case VENTAS_EXCEL ->
                    "Hoja membretada - Excel";

            case INVENTARIO_WORD ->
                    "Inventario - Word";

            case INVENTARIO_EXCEL ->
                    "Inventario - Excel";

        };

    }

    // ==========================================
    // ELIMINAR ARCHIVO FÍSICO
    // ==========================================

    private void eliminarArchivoFisico(String ruta) {

        if (ruta == null || ruta.isBlank()) {
            return;
        }

        try {

            Path directorio = Paths.get(directorioPlantillas)
                    .toAbsolutePath()
                    .normalize();

            Path archivo = Paths.get(ruta)
                    .toAbsolutePath()
                    .normalize();

            // Solo eliminar archivos de nuestra carpeta
            if (!archivo.startsWith(directorio)) {
                throw new RuntimeException(
                        "No se permite eliminar archivos fuera de plantillas."
                );
            }

            Files.deleteIfExists(archivo);

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo eliminar el archivo anterior.",
                    e
            );

        }

    }

}
