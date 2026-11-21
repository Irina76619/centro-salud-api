-- Solo si YA creaste la BD con el script original (claves en texto plano).
-- Recomendado: vuelve a ejecutar database/centro_salud.sql completo (borra y recrea la BD con claves BCrypt).
-- Si prefieres conservar tus datos, amplia las columnas y luego actualiza las claves con hashes BCrypt:
USE centro_salud;
ALTER TABLE medico MODIFY clave VARCHAR(100) NOT NULL;
ALTER TABLE administrativo MODIFY clave VARCHAR(100) NOT NULL;
