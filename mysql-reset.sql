ALTER USER 'root'@'localhost' IDENTIFIED BY 'SkateRoot_2026!Change';
CREATE USER IF NOT EXISTS 'skateapp'@'%' IDENTIFIED BY 'trongvinh_2k6';
ALTER USER 'skateapp'@'%' IDENTIFIED BY 'trongvinh_2k6';
GRANT ALL PRIVILEGES ON projectskateboard.* TO 'skateapp'@'%';
