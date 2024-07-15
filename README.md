Script de la base de datos de IGNIS:

CREATE TABLE Niveles(
id_nivelUsuario INT GENERATED ALWAYS AS IDENTITY,
nombre_nivel VARCHAR2(10) NOT NULL,

CONSTRAINT pk_niveles PRIMARY KEY (id_nivelUsuario)
);

CREATE TABLE Usuarios (
id_usuario INT GENERATED ALWAYS AS IDENTITY,
nombre_usuario VARCHAR2(25) NOT NULL UNIQUE,
contrasena_usuario VARCHAR2(255) NOT NULL,
edad_usuario INT NOT NULL,
dui_usuario VARCHAR2(10) UNIQUE,
id_nivelUsuario INT NOT NULL,


CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
CONSTRAINT fk_niveles FOREIGN KEY (id_nivelUsuario) REFERENCES Niveles (id_nivelUsuario) ON DELETE CASCADE,
CONSTRAINT check_edad CHECK (edad_usuario > 12)
);

CREATE TABLE Cambios_Sistema (
id_cambioSistema INT GENERATED ALWAYS AS IDENTITY,
descripcion_cambio VARCHAR2(100) NOT NULL,	
fecha_cambio DATE NOT NULL,
id_usuario INT NOT NULL,


CONSTRAINT pk_cambios PRIMARY KEY (id_cambioSistema),
CONSTRAINT fk_usuarios FOREIGN KEY (id_usuario) REFERENCES Usuarios (id_usuario) ON DELETE CASCADE   
);

CREATE TABLE Bomberos (
id_bombero INT GENERATED ALWAYS AS IDENTITY,
nombre_bombero VARCHAR2(25) NOT NULL,
apellido_bombero VARCHAR2(25) NOT NULL,
experiencia_bombero VARCHAR2(250) NOT NULL,
especializacion_bombero VARCHAR2(20) NOT NULL,
foto_bombero CLOB NOT NULL,
id_usuario INT NOT NULL,

CONSTRAINT pk_bomberos PRIMARY KEY (id_bombero),
CONSTRAINT fk_usuariosbomberos FOREIGN KEY (id_usuario) REFERENCES Usuarios (id_usuario) ON DELETE CASCADE

);

CREATE TABLE Aspirantes (
id_aspirante INT GENERATED ALWAYS AS IDENTITY,
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


CREATE TABLE Emergencias(
id_emergencia INT GENERATED ALWAYS AS IDENTITY,
ubicacion_emergencia VARCHAR2(100) NOT NULL,
descripcion_emergencia VARCHAR2(250) NOT NULL,
gravedad_emergencia VARCHAR2(20) NOT NULL,
tipo_emergencia VARCHAR2(20) NOT NULL,
respuesta_notificacion VARCHAR2(10),
estado_emergencia VARCHAR2(10) NOT NULL,

CONSTRAINT pk_emergencias PRIMARY KEY (id_emergencia)
);


CREATE TABLE Misiones (
id_mision INT GENERATED ALWAYS AS IDENTITY,
descripcion_mision VARCHAR2(150) NOT NULL,
fecha_mision DATE NOT NULL,
id_emergencia INT NOT NULL,

CONSTRAINT pk_misiones PRIMARY KEY (id_mision),
CONSTRAINT fk_emergencias FOREIGN KEY (id_emergencia) REFERENCES Emergencias(id_emergencia) ON DELETE CASCADE
);

CREATE TABLE Informes(
id_informe INT GENERATED ALWAYS AS IDENTITY,
id_mision INT NOT NULL,
resultado_mision VARCHAR2(10) NOT NULL,
descripcion_mision VARCHAR2(250)NOT NULL,

CONSTRAINT pk_informes PRIMARY KEY (id_informe),
CONSTRAINT fk_misiones FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE
); 

CREATE TABLE Recursos (
id_recurso INT GENERATED ALWAYS AS IDENTITY,
nombre_recurso VARCHAR2(15) NOT NULL,
descripcion_recurso VARCHAR2(150) NOT NULL,
estado_recurso VARCHAR2(10) NOT NULL,
fechaRecepcion_recurso DATE NOT NULL,
disponibilidad_recurso VARCHAR2(10) NOT NULL,
foto_recurso CLOB NOT NULL,

CONSTRAINT pk_recursos PRIMARY KEY (id_recurso)
);

CREATE TABLE Misiones_Recursos (
id_misionRecurso INT GENERATED ALWAYS AS IDENTITY,
id_mision INT NOT NULL,
id_recurso INT NOT NULL,

CONSTRAINT pk_misionesrecursos PRIMARY KEY (id_misionRecurso),
CONSTRAINT fk_misionesyrecursos FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE,
CONSTRAINT fk_recursosmisiones FOREIGN KEY (id_recurso) REFERENCES Recursos(id_recurso) ON DELETE CASCADE
);

CREATE TABLE Misiones_Bomberos (
id_misionBombero INT GENERATED ALWAYS AS IDENTITY,
id_mision INT NOT NULL,
id_bombero INT NOT NULL,

CONSTRAINT pk_misionesbomberos PRIMARY KEY (id_misionBombero),
CONSTRAINT fk_misionesybomberos FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE,
CONSTRAINT fk_bomberosmisiones FOREIGN KEY (id_bombero) REFERENCES Bomberos (id_bombero) ON DELETE CASCADE
);	

CREATE TABLE Transportes (
id_transporte INT GENERATED ALWAYS AS IDENTITY,
placa_transporte VARCHAR2(12) NOT NULL UNIQUE,
numero_transporte VARCHAR2(4) NOT NULL UNIQUE,
capacidad_transporte NUMBER NOT NULL,
tipoVehiculo_transporte VARCHAR2(10) NOT NULL,
disponibilidad_transporte VARCHAR2(10) NOT NULL,
estado_transporte VARCHAR2(10) NOT NULL,

CONSTRAINT pk_transportes PRIMARY KEY (id_transporte)
);

CREATE TABLE Misiones_Transportes (
id_misionTransporte INT GENERATED ALWAYS AS IDENTITY,
id_mision INT NOT NULL,
id_transporte INT NOT NULL,

CONSTRAINT pk_misionestransportes PRIMARY KEY (id_misionTransporte),
CONSTRAINT fk_misionestransportes FOREIGN KEY (id_mision) REFERENCES Misiones (id_mision) ON DELETE CASCADE,
CONSTRAINT fk_transportes FOREIGN KEY (id_transporte) REFERENCES Transportes (id_transporte) ON DELETE CASCADE
);

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

