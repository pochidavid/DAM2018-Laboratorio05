package ar.edu.utn.frsf.isi.dam.laboratorio05.modelo;

import android.arch.persistence.room.TypeConverter;

public class   TipoReclamoConverter {

        @TypeConverter
        public static Reclamo.TipoReclamo  toEstado(String status) {
            return Reclamo.TipoReclamo .valueOf(status);
        }

        @TypeConverter
        public static String  toString(Reclamo.TipoReclamo status) {
            return status.toString();
        }
    }