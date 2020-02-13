/* INSERE FILMES */
INSERT INTO Filme(id, titulo, ano, poster, genero, cadastrado_em) 
     VALUES(1, 'Avengers: Infinity War', 2019, 'https://image.tmdb.org/t/p/original/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg', 'ação', {ts '2020-02-07 16:35:52.69'}); 

 INSERT INTO Filme(id, titulo, ano, poster, genero, cadastrado_em) 
     VALUES(2, 'My Girl', 1991, 'https://image.tmdb.org/t/p/original/4Csti4PTqzJM1lSY1zoFgXyjB8X.jpg', 'romance', {ts '2020-02-07 16:35:52.69'}); 
     
INSERT INTO Filme(id, titulo, ano, poster, genero, cadastrado_em) 
     VALUES(3, 'Annie Hall', 1977, 'https://image.tmdb.org/t/p/original/bK9bLXwxWuXX5mbq75PZqjvAtfG.jpg', 'romance', {ts '2020-02-07 16:35:52.69'}); 

/* INSERE TRADUÇÕES */     
INSERT INTO Traducao(filme_id, codigo, idioma, titulo, poster) 
     VALUES(1, 'es', 'Espanhol', 'Vengadores: Infinity War', 'https://image.tmdb.org/t/p/original/ksBQ4oHQDdJwND8H90ay8CbMihU.jpg');
     
INSERT INTO Traducao(filme_id, codigo, idioma, titulo, poster) 
     VALUES(1, 'pt-br', 'Português do Brasil', 'Vingadores: Guerra Infinita', 'https://image.tmdb.org/t/p/w220_and_h330_face/rkHe0BfOo1f5N2q6rxgdYac7Zf6.jpg');       
     
INSERT INTO Traducao(filme_id, codigo, idioma, titulo, poster) 
     VALUES(3, 'pt-br', 'Português do Brasil', 'Noivo neurótico, noiva nervosa', 'https://image.tmdb.org/t/p/original/t0GRKyQCN9483OD9PLZTcNUCfEV.jpg');     