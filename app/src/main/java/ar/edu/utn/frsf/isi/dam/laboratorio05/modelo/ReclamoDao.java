package ar.edu.utn.frsf.isi.dam.laboratorio05.modelo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ReclamoDao {
    @Query("SELECT * FROM Reclamo")
    List<Reclamo> getAll();

    @Query("SELECT * FROM Reclamo WHERE tipo = :pTipo")
    List<Reclamo> getByTipo(String pTipo);

    @Query("SELECT * FROM Reclamo WHERE id = :pIdReclamo")
    Reclamo getById(int pIdReclamo);

    @Insert
    long insert(Reclamo r);

    @Insert
    void update(Reclamo r);

    @Delete
    void delete(Reclamo r);
}
