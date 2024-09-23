--Tabla dedicada a almacenar los niveles de usuario de IGNIS--
CREATE TABLE Niveles(
id_nivelUsuario INT GENERATED BY DEFAULT AS IDENTITY,
nombre_nivel VARCHAR2(10) NOT NULL,

CONSTRAINT pk_niveles PRIMARY KEY (id_nivelUsuario)
);


--Tabla dedicada a almacenar a los usuarios registrados dentro del sistema--
CREATE TABLE Usuarios (
id_usuario INT GENERATED BY DEFAULT AS IDENTITY,
nombre_usuario VARCHAR2(25) NOT NULL UNIQUE,
contrasena_usuario VARCHAR2(255) NOT NULL,
edad_usuario INT NOT NULL,
dui_usuario VARCHAR2(10) UNIQUE,
id_nivelUsuario INT NOT NULL,


CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
CONSTRAINT fk_niveles FOREIGN KEY (id_nivelUsuario) REFERENCES Niveles (id_nivelUsuario) ON DELETE CASCADE,
CONSTRAINT check_edad CHECK (edad_usuario > 12)
);

--Tabla para guardar los cambios importantes realizados en la aplicaicon de escritorio o m?vil--
CREATE TABLE Cambios_Sistema (
id_cambioSistema INT GENERATED BY DEFAULT AS IDENTITY,
descripcion_cambio VARCHAR2(100) NOT NULL,	
fecha_cambio DATE NOT NULL,
id_usuario INT NOT NULL,


CONSTRAINT pk_cambios PRIMARY KEY (id_cambioSistema),
CONSTRAINT fk_usuarios FOREIGN KEY (id_usuario) REFERENCES Usuarios (id_usuario) ON DELETE CASCADE   
);

--Tabla para almacenar a los bomberos de la estacion que corresponda--
CREATE TABLE Bomberos (
id_bombero INT GENERATED BY DEFAULT AS IDENTITY,
nombre_bombero VARCHAR2(25) NOT NULL,
apellido_bombero VARCHAR2(25) NOT NULL,
experiencia_bombero VARCHAR2(250) NOT NULL,
especializacion_bombero VARCHAR2(20) NOT NULL,
foto_bombero CLOB NOT NULL,
id_usuario INT NOT NULL,

CONSTRAINT pk_bomberos PRIMARY KEY (id_bombero),
CONSTRAINT fk_usuariosbomberos FOREIGN KEY (id_usuario) REFERENCES Usuarios (id_usuario) ON DELETE CASCADE

);

--Tabla creada para almacenar a los aspirantes a bomberos y toda la informaci?n pertinente sobre estos--
CREATE TABLE Aspirantes (
id_aspirante INT GENERATED BY DEFAULT AS IDENTITY,
nombre_aspirante VARCHAR2(25) NOT NULL,
apellido_aspirante VARCHAR2(25) NOT NULL,
dui_aspirante VARCHAR(10) NOT NULL UNIQUE,
entrenamiento_aspirante VARCHAR2(200) NOT NULL,
edad_usuario INT NOT NULL,   
progreso_aspirante VARCHAR2(250) NOT NULL,
foto_aspirante CLOB NOT NULL,
id_bombero INT NOT NULL,

CONSTRAINT pk_aspirantes PRIMARY KEY (id_aspirante),
CONSTRAINT fk_bomberosaspirantes FOREIGN KEY (id_bombero) REFERENCES Bomberos (id_bombero) ON DELETE CASCADE,
CONSTRAINT check_edadaspirantes CHECK (edad_usuario > 12)


);


--Tabla para gestionar y almacenar las emergencias enviadas por parte de los usuarios--
CREATE TABLE Emergencias(
id_emergencia INT GENERATED BY DEFAULT AS IDENTITY,
ubicacion_emergencia VARCHAR2(100) NOT NULL,
descripcion_emergencia VARCHAR2(250) NOT NULL,
gravedad_emergencia VARCHAR2(20) NOT NULL,
tipo_emergencia VARCHAR2(20) NOT NULL,
respuesta_notificacion VARCHAR2(10),
estado_emergencia VARCHAR2(10) NOT NULL,

CONSTRAINT pk_emergencias PRIMARY KEY (id_emergencia)
);


--Tabla para generar misiones de bomberos--
CREATE TABLE Misiones (
id_mision INT GENERATED BY DEFAULT AS IDENTITY,
descripcion_mision VARCHAR2(150) NOT NULL,
fecha_mision DATE NOT NULL,
id_emergencia INT NOT NULL,

CONSTRAINT pk_misiones PRIMARY KEY (id_mision),
CONSTRAINT fk_emergencias FOREIGN KEY (id_emergencia) REFERENCES Emergencias(id_emergencia) ON DELETE CASCADE
);

--Tabla para almacenar los informes de las misiones enviados por los bomberos--
CREATE TABLE Informes(
id_informe INT GENERATED BY DEFAULT AS IDENTITY,
id_mision INT NOT NULL,
resultado_mision VARCHAR2(10) NOT NULL,
descripcion_mision VARCHAR2(250)NOT NULL,

CONSTRAINT pk_informes PRIMARY KEY (id_informe),
CONSTRAINT fk_misiones FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE
); 

--Tabla para almacenar los recursos que se encuentran dentro de la estaci?n--
CREATE TABLE Recursos (
id_recurso INT GENERATED BY DEFAULT AS IDENTITY,
nombre_recurso VARCHAR2(15) NOT NULL,
descripcion_recurso VARCHAR2(150) NOT NULL,
estado_recurso VARCHAR2(10) NOT NULL,
fechaRecepcion_recurso DATE NOT NULL,
disponibilidad_recurso VARCHAR2(10) NOT NULL,
foto_recurso CLOB NOT NULL,

CONSTRAINT pk_recursos PRIMARY KEY (id_recurso)
);

--Tabla intermedia entre misiones y recursos para asignar recursos espec?ficos a misiones espe?ificas--
CREATE TABLE Misiones_Recursos (
id_misionRecurso INT GENERATED BY DEFAULT AS IDENTITY,
id_mision INT NOT NULL,
id_recurso INT NOT NULL,

CONSTRAINT pk_misionesrecursos PRIMARY KEY (id_misionRecurso),
CONSTRAINT fk_misionesyrecursos FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE,
CONSTRAINT fk_recursosmisiones FOREIGN KEY (id_recurso) REFERENCES Recursos(id_recurso) ON DELETE CASCADE
);

--Tabla intermedia entre misiones y bomberos para asignar bomberos a misiones--
CREATE TABLE Misiones_Bomberos (
id_misionBombero INT GENERATED BY DEFAULT AS IDENTITY,
id_mision INT NOT NULL,
id_bombero INT NOT NULL,

CONSTRAINT pk_misionesbomberos PRIMARY KEY (id_misionBombero),
CONSTRAINT fk_misionesybomberos FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE,
CONSTRAINT fk_bomberosmisiones FOREIGN KEY (id_bombero) REFERENCES Bomberos (id_bombero) ON DELETE CASCADE
);	


--Tabla que almacena los transportes disponibles dentro de la estaci?n de bomberos--
CREATE TABLE Transportes (
id_transporte INT GENERATED BY DEFAULT AS IDENTITY,
placa_transporte VARCHAR2(12) NOT NULL UNIQUE,
numero_transporte VARCHAR2(4) NOT NULL UNIQUE,
capacidad_transporte NUMBER NOT NULL,
tipoVehiculo_transporte VARCHAR2(50) NOT NULL,
disponibilidad_transporte VARCHAR2(50) NOT NULL,
estado_transporte VARCHAR2(50) NOT NULL,

CONSTRAINT pk_transportes PRIMARY KEY (id_transporte)
);

--Tabla intermedia entre misiones y transportes donde se guardan los transportes asignados a las misiones--
CREATE TABLE Misiones_Transportes (
id_misionTransporte INT GENERATED BY DEFAULT AS IDENTITY,
id_mision INT NOT NULL,
id_transporte INT NOT NULL,

CONSTRAINT pk_misionestransportes PRIMARY KEY (id_misionTransporte),
CONSTRAINT fk_misionestransportes FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE,
CONSTRAINT fk_transportes FOREIGN KEY (id_transporte) REFERENCES Transportes (id_transporte) ON DELETE CASCADE
);


--Tabla auditoria creada para almacenar los datos de los cambios realizados en la base de datos--
CREATE TABLE Auditoria (
    id_auditoria INT,
    tabla_afectada VARCHAR2(50) NOT NULL,
    id_afectado INT,
    operacion VARCHAR2(10) NOT NULL,
    descripcion_cambio VARCHAR2(255) NOT NULL,
    fecha_cambio TIMESTAMP DEFAULT SYSDATE,
    usuario_nombre VARCHAR2(30)  
    
);


--Secuencia para el identificador de la tabla auditoria--
CREATE SEQUENCE seq_auditoria
START WITH 1
INCREMENT BY 1;


--Procedimiento almacenado para realizar un INSERT dentro de la tabla auditoria, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE PROCEDURE registrar_auditoria (
    p_tabla_afectada IN Auditoria.tabla_afectada%TYPE,
    p_id_afectado IN Auditoria.id_afectado%TYPE,
    p_operacion IN Auditoria.operacion%TYPE,
    p_descripcion_cambio IN Auditoria.descripcion_cambio%TYPE
) IS
BEGIN
    INSERT INTO Auditoria (
        id_auditoria, 
        tabla_afectada, 
        id_afectado, 
        operacion, 
        descripcion_cambio, 
        fecha_cambio, 
        usuario_nombre
    ) VALUES (
        seq_auditoria.NEXTVAL, 
        p_tabla_afectada, 
        p_id_afectado, 
        p_operacion, 
        p_descripcion_cambio, 
        SYSDATE, 
        user
    );
END;

--Trigger que se dispara al momento de realizar un cambio dentro de la tabla niveles, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_niveles
AFTER INSERT OR UPDATE OR DELETE ON Niveles
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Niveles',
            :NEW.id_nivelUsuario,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Niveles',
            :NEW.id_nivelUsuario,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Niveles',
            :OLD.id_nivelUsuario,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un cambio dentro de la tabla transportes, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_transportes
AFTER INSERT OR UPDATE OR DELETE ON Transportes
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Transportes',
            :NEW.id_transporte,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Transportes',
            :NEW.id_transporte,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Transportes',
            :OLD.id_transporte,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un cambio dentro de la tabla cambios_sistema, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_cambios_sistema
AFTER INSERT OR UPDATE OR DELETE ON Cambios_Sistema
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Cambios_Sistema',
            :NEW.id_cambioSistema,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Cambios_Sistema',
            :NEW.id_cambioSistema,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Cambios_Sistema',
            :OLD.id_cambioSistema,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un cambio dentro de la tabla usuarios, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_usuarios
AFTER INSERT OR UPDATE OR DELETE ON Usuarios
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Usuarios',
            :NEW.id_usuario,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Usuarios',
            :NEW.id_usuario,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Usuarios',
            :OLD.id_usuario,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un cambio dentro de la tabla bomberos, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_bomberos
AFTER INSERT OR UPDATE OR DELETE ON Bomberos
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Bomberos',
            :NEW.id_bombero,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Bomberos',
            :NEW.id_bombero,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Bomberos',
            :OLD.id_bombero,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un cambio dentro de la tabla aspirantes, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_aspirantes
AFTER INSERT OR UPDATE OR DELETE ON Aspirantes
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Aspirantes',
            :NEW.id_aspirante,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Aspirantes',
            :NEW.id_aspirante,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Aspirantes',
            :OLD.id_aspirante,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un dentro de la tabla emergencias, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_emergencias
AFTER INSERT OR UPDATE OR DELETE ON Emergencias
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Emergencias',
            :NEW.id_emergencia,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Emergencias',
            :NEW.id_emergencia,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Emergencias',
            :OLD.id_emergencia,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;


--Trigger que se dispara al momento de realizar un dentro de la tabla misiones, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_misiones
AFTER INSERT OR UPDATE OR DELETE ON Misiones
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Misiones',
            :NEW.id_mision,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Misiones',
            :NEW.id_mision,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Misiones',
            :OLD.id_mision,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un dentro de la tabla informes, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_informes
AFTER INSERT OR UPDATE OR DELETE ON Informes
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Informes',
            :NEW.id_informe,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Informes',
            :NEW.id_informe,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Informes',
            :OLD.id_informe,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un dentro de la tabla recursos, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_recursos
AFTER INSERT OR UPDATE OR DELETE ON Recursos
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Recursos',
            :NEW.id_recurso,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Recursos',
            :NEW.id_recurso,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Recursos',
            :OLD.id_recurso,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un dentro de la tabla misiones_recursos, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_misiones_recursos
AFTER INSERT OR UPDATE OR DELETE ON Misiones_Recursos
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Misiones_Recursos',
            :NEW.id_misionRecurso,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Misiones_Recursos',
            :NEW.id_misionRecurso,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Misiones_Recursos',
            :OLD.id_misionRecurso,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un dentro de la tabla misiones_bomberos, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_misiones_bomberos
AFTER INSERT OR UPDATE OR DELETE ON Misiones_Bomberos
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Misiones_Bomberos',
            :NEW.id_misionBombero,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Misiones_Bomberos',
            :NEW.id_misionBombero,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Misiones_Bomberos',
            :OLD.id_misionBombero,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Trigger que se dispara al momento de realizar un dentro de la tabla misiones_transportes, insertando la informaci?n en la tabla auditoria--
CREATE OR REPLACE TRIGGER trg_auditoria_misiones_transportes
AFTER INSERT OR UPDATE OR DELETE ON Misiones_Transportes
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        registrar_auditoria(
            'Misiones_Transportes',
            :NEW.id_misionTransporte,
            'INSERT',
            'Registro insertado.'
        );
    ELSIF UPDATING THEN
        registrar_auditoria(
            'Misiones_Transportes',
            :NEW.id_misionTransporte,
            'UPDATE',
            'Registro actualizado.'
        );
    ELSIF DELETING THEN
        registrar_auditoria(
            'Misiones_Transportes',
            :OLD.id_misionTransporte,
            'DELETE',
            'Registro eliminado.'
        );
    END IF;
END;

--Procedimiento almacenado para eliminar un usuario de la base de datos--
CREATE OR REPLACE PROCEDURE eliminar_usuario(
    p_id_usuario IN INT
) AS
BEGIN
    DELETE FROM Usuarios
    WHERE id_usuario = p_id_usuario;
    
    COMMIT;
END;

--Procedimiento almacenado para eliminar en funcionamiento--
BEGIN 
    eliminar_usuario(4); 
END;


    SELECT * FROM Auditoria;

DROP SEQUENCE seq_auditoria;

--Drops tables si es necesario eliminar los datos de la base de datos e iniciar una nueva--

-- Eliminar triggers
DROP TRIGGER trg_auditoria_niveles;
DROP TRIGGER trg_auditoria_transportes;

-- Eliminar secuencia
DROP SEQUENCE seq_auditoria;

-- Eliminar tablas intermedias primero (aquellas que tienen claves for neas a otras tablas)
DROP TABLE Misiones_Transportes CASCADE CONSTRAINTS;
DROP TABLE Misiones_Bomberos CASCADE CONSTRAINTS;
DROP TABLE Misiones_Recursos CASCADE CONSTRAINTS;

-- Eliminar tablas dependientes
DROP TABLE Aspirantes CASCADE CONSTRAINTS;
DROP TABLE Bomberos CASCADE CONSTRAINTS;
DROP TABLE Cambios_Sistema CASCADE CONSTRAINTS;
DROP TABLE Informes CASCADE CONSTRAINTS;
DROP TABLE Misiones CASCADE CONSTRAINTS;
DROP TABLE Emergencias CASCADE CONSTRAINTS;
DROP TABLE Recursos CASCADE CONSTRAINTS;
DROP TABLE Transportes CASCADE CONSTRAINTS;
DROP TABLE Usuarios CASCADE CONSTRAINTS;
DROP TABLE Niveles CASCADE CONSTRAINTS;

-- Eliminar la tabla de auditor a al final ya que puede depender de otras tablas
DROP TABLE Auditoria CASCADE CONSTRAINTS;

SELECT * FROM auditoria;


--Tabla usuarios
INSERT INTO Usuarios (nombre_usuario, contrasena_usuario, edad_usuario, dui_usuario, id_nivelUsuario) 
VALUES ('jdoe', 'password123', 30, '91234567-8', 1);

INSERT INTO Usuarios (nombre_usuario, contrasena_usuario, edad_usuario, dui_usuario, id_nivelUsuario) 
VALUES ('asmith', 'securepass', 25, '01234567-8', 2);

INSERT INTO Usuarios (nombre_usuario, contrasena_usuario, edad_usuario, dui_usuario, id_nivelUsuario) 
VALUES ('mbrown', 'mypassword', 28, '09876543-1', 3);

INSERT INTO Usuarios (nombre_usuario, contrasena_usuario, edad_usuario, dui_usuario, id_nivelUsuario) 
VALUES ('cjones', 'password321', 35, '76543987-3', 4);

--Tabla Cambios_sistema
INSERT INTO Cambios_Sistema (descripcion_cambio, fecha_cambio, id_usuario) 
VALUES ('Actualización de seguridad en la aplicación', TO_DATE('2024-09-01', 'YYYY-MM-DD'), 1);

INSERT INTO Cambios_Sistema (descripcion_cambio, fecha_cambio, id_usuario) 
VALUES ('Mejora en la interfaz de usuario', TO_DATE('2024-09-02', 'YYYY-MM-DD'), 2);

INSERT INTO Cambios_Sistema (descripcion_cambio, fecha_cambio, id_usuario) 
VALUES ('Corrección de errores en el módulo de reportes', TO_DATE('2024-09-03', 'YYYY-MM-DD'), 3);

INSERT INTO Cambios_Sistema (descripcion_cambio, fecha_cambio, id_usuario) 
VALUES ('Implementación de nuevas funcionalidades', TO_DATE('2024-09-04', 'YYYY-MM-DD'), 4);

--Tabla Bomberos
INSERT INTO Bomberos (nombre_bombero, apellido_bombero, experiencia_bombero, especializacion_bombero, foto_bombero, id_usuario) 
VALUES ('Juan', 'Pérez', '5 años en incendios forestales', 'Rescate', 'foto1.jpg', 1);

INSERT INTO Bomberos (nombre_bombero, apellido_bombero, experiencia_bombero, especializacion_bombero, foto_bombero, id_usuario) 
VALUES ('Ana', 'Gómez', '3 años en emergencias urbanas', 'Emergencias', 'foto2.jpg', 2);

INSERT INTO Bomberos (nombre_bombero, apellido_bombero, experiencia_bombero, especializacion_bombero, foto_bombero, id_usuario) 
VALUES ('Luis', 'Martínez', '7 años en rescates acuáticos', 'Rescate', 'foto3.jpg', 3);

INSERT INTO Bomberos (nombre_bombero, apellido_bombero, experiencia_bombero, especializacion_bombero, foto_bombero, id_usuario) 
VALUES ('Sofia', 'Ruiz', '4 años en incendios industriales', 'Industria', 'foto4.jpg', 4);

--Tabla Aspirantes
INSERT INTO Aspirantes (nombre_aspirante, apellido_aspirante, dui_aspirante, entrenamiento_aspirante, edad_usuario, progreso_aspirante, foto_aspirante, id_bombero) 
VALUES ('Carlos', 'Sánchez', '12345678', 'Entrenamiento básico', 20, 'Progreso inicial', 'foto1.jpg', 1);

INSERT INTO Aspirantes (nombre_aspirante, apellido_aspirante, dui_aspirante, entrenamiento_aspirante, edad_usuario, progreso_aspirante, foto_aspirante, id_bombero) 
VALUES ('María', 'Fernández', '23456789', 'Entrenamiento avanzado', 22, 'En proceso', 'foto2.jpg', 2);

INSERT INTO Aspirantes (nombre_aspirante, apellido_aspirante, dui_aspirante, entrenamiento_aspirante, edad_usuario, progreso_aspirante, foto_aspirante, id_bombero) 
VALUES ('José', 'Pineda', '34567890', 'Entrenamiento medio', 21, 'Avanzado', 'foto3.jpg', 3);

INSERT INTO Aspirantes (nombre_aspirante, apellido_aspirante, dui_aspirante, entrenamiento_aspirante, edad_usuario, progreso_aspirante, foto_aspirante, id_bombero) 
VALUES ('Laura', 'Jiménez', '45678901', 'Entrenamiento intermedio', 23, 'Finalizado', 'foto4.jpg', 4);

--Tabla Transportes
INSERT INTO Transportes (placa_transporte, numero_transporte, capacidad_transporte, tipoVehiculo_transporte, disponibilidad_transporte, estado_transporte) 
VALUES ('ABC1234', '001', 10, 'Camión', 'Disponible', 'Operativo');

INSERT INTO Transportes (placa_transporte, numero_transporte, capacidad_transporte, tipoVehiculo_transporte, disponibilidad_transporte, estado_transporte) 
VALUES ('DEF5678', '002', 10, 'Pickup', 'Disponible', 'Operativo');

INSERT INTO Transportes (placa_transporte, numero_transporte, capacidad_transporte, tipoVehiculo_transporte, disponibilidad_transporte, estado_transporte) 
VALUES ('GHI9101', '003', 10, 'Pickup', 'disponible', 'Inactivo');

INSERT INTO Transportes (placa_transporte, numero_transporte, capacidad_transporte, tipoVehiculo_transporte, disponibilidad_transporte, estado_transporte) 
VALUES ('JKL2345', '004', 10, 'Camión', 'Disponible', 'Operativo');

--Tabla Emergencias
INSERT INTO Emergencias (ubicacion_emergencia, descripcion_emergencia, gravedad_emergencia, tipo_emergencia, respuesta_notificacion, estado_emergencia) 
VALUES ('Sector 5, Calle Principal', 'Incendio en edificio de oficinas', 'Alta', 'Incendio', 'Pendiente', 'Activa');

INSERT INTO Emergencias (ubicacion_emergencia, descripcion_emergencia, gravedad_emergencia, tipo_emergencia, respuesta_notificacion, estado_emergencia) 
VALUES ('Barrio San José', 'Accidente vehicular con heridos', 'Media', 'Accidente', 'Notificada', 'Activa');

INSERT INTO Emergencias (ubicacion_emergencia, descripcion_emergencia, gravedad_emergencia, tipo_emergencia, respuesta_notificacion, estado_emergencia) 
VALUES ('Zona Industrial', 'Explosión en planta química', 'Alta', 'Explosión', 'En camino', 'Activa');

INSERT INTO Emergencias (ubicacion_emergencia, descripcion_emergencia, gravedad_emergencia, tipo_emergencia, respuesta_notificacion, estado_emergencia) 
VALUES ('Parque Central', 'Fuga de gas', 'Baja', 'Fuga', 'Resuelta', 'Inactiva');

--Tabla Recursos
INSERT INTO Recursos (nombre_recurso, descripcion_recurso, estado_recurso, fechaRecepcion_recurso, disponibilidad_recurso, foto_recurso) 
VALUES ('Manguera', 'Manguera de 20 metros para incendios', 'Buena', TO_DATE('2024-08-01', 'YYYY-MM-DD'), 'Disponible', 'foto_recurso1.jpg');

INSERT INTO Recursos (nombre_recurso, descripcion_recurso, estado_recurso, fechaRecepcion_recurso, disponibilidad_recurso, foto_recurso) 
VALUES ('Extintor', 'Extintor de espuma de 10 litros', 'Bueno', TO_DATE('2024-08-15', 'YYYY-MM-DD'), 'Disponible', 'foto_recurso2.jpg');

INSERT INTO Recursos (nombre_recurso, descripcion_recurso, estado_recurso, fechaRecepcion_recurso, disponibilidad_recurso, foto_recurso) 
VALUES ('Escalera', 'Escalera de aluminio de 3 metros', 'Usado', TO_DATE('2024-07-20', 'YYYY-MM-DD'), 'Disponible', 'foto_recurso3.jpg');

INSERT INTO Recursos (nombre_recurso, descripcion_recurso, estado_recurso, fechaRecepcion_recurso, disponibilidad_recurso, foto_recurso) 
VALUES ('Botiquin', 'Kit completo para primeros auxilios', 'Nuevo', TO_DATE('2024-08-30', 'YYYY-MM-DD'), 'Disponible', 'foto_recurso4.jpg');

--Tabla Misiones
INSERT INTO Misiones (descripcion_mision, fecha_mision, id_emergencia) 
VALUES ('Control de incendio en edificio de oficinas', TO_DATE('2024-09-01', 'YYYY-MM-DD'), 1);

INSERT INTO Misiones (descripcion_mision, fecha_mision, id_emergencia) 
VALUES ('Atención a accidente vehicular con heridos', TO_DATE('2024-09-02', 'YYYY-MM-DD'), 2);

INSERT INTO Misiones (descripcion_mision, fecha_mision, id_emergencia) 
VALUES ('Respuesta a explosión en planta química', TO_DATE('2024-09-03', 'YYYY-MM-DD'), 3);

INSERT INTO Misiones (descripcion_mision, fecha_mision, id_emergencia) 
VALUES ('Manejo de fuga de gas en parque central', TO_DATE('2024-09-04', 'YYYY-MM-DD'), 4);

--Tabla Misiones_Recursos
INSERT INTO Misiones_Recursos (id_mision, id_recurso) 
VALUES (1, 1);

INSERT INTO Misiones_Recursos (id_mision, id_recurso) 
VALUES (2, 2);

INSERT INTO Misiones_Recursos (id_mision, id_recurso) 
VALUES (3, 3);

INSERT INTO Misiones_Recursos (id_mision, id_recurso) 
VALUES (4, 4);


--Tabla Misiones_bomberos
INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (1, 1);

INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (2, 2);

INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (3, 3);

INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (4, 4);

--Tabla misiones_transportes
INSERT INTO Misiones_Transportes (id_mision, id_transporte) 
VALUES (1, 1);

INSERT INTO Misiones_Transportes (id_mision, id_transporte) 
VALUES (2, 2);

INSERT INTO Misiones_Transportes (id_mision, id_transporte) 
VALUES (3, 3);

INSERT INTO Misiones_Transportes (id_mision, id_transporte) 
VALUES (4, 4);





--Tabla Misiones bomberos
INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (1, 1);

INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (2, 2);

INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (3, 3);

INSERT INTO Misiones_Bomberos (id_mision, id_bombero) 
VALUES (4, 4);

--inner join1-- 
SELECT U.nombre_usuario AS Usuario, CS.descripcion_cambio AS Descripcion, CS.fecha_cambio AS Fecha From Cambios_Sistema CS
INNER JOIN Usuarios U on CS.id_usuario = U.id_usuario

--inner join2-- 
SELECT M.descripcion_mision AS Mision, M.fecha_mision AS Fecha, T.placa_transporte AS PlacaTransporte, T.tipoVehiculo_transporte AS Vehiculo From Misiones_Transportes MT
INNER JOIN Misiones M on MT.id_mision = M.id_mision
INNER JOIN Transportes T on MT.id_transporte = T.id_transporte

--inner join3-- 
SELECT M.descripcion_mision AS Mision, M.fecha_mision as Fecha, B.nombre_bombero || ' ' || B.apellido_bombero AS Bombero, B.especializacion_bombero AS Especializacion From Misiones_Bomberos MB
INNER JOIN Misiones M on MB.id_mision = M.id_mision
INNER JOIN Bomberos B on MB.id_bombero = B.id_bombero

SELECT * FROM Transportes;
