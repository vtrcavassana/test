package br.usjt.usjt_ccp3_consumo_img_init.model.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import br.usjt.usjt_ccp3_consumo_img_init.model.dao.FilmeDAO;
import br.usjt.usjt_ccp3_consumo_img_init.model.entity.Filme;
import br.usjt.usjt_ccp3_consumo_img_init.model.entity.Genero;
import br.usjt.usjt_ccp3_consumo_img_init.model.javabeans.Lancamentos;
import br.usjt.usjt_ccp3_consumo_img_init.model.javabeans.Movie;
import br.usjt.usjt_ccp3_consumo_img_init.model.javabeans.Populares;

@Service
public class FilmeService {
	
	private static final String API_KEY = "a90c89a4d8af92302cf5594a8c460900";
	public static final String BASE_URL = "https://api.themoviedb.org/3";
	public static final String LANG = "pt-BR";
	public static final String POPULAR = "/movie/popular?api_key="+API_KEY+"&language="+LANG;
	public static final String UPCOMING = "/movie/upcoming?api_key="+API_KEY+"&language="+LANG;
	public static final String POSTER_URL = "https://image.tmdb.org/t/p/w300";

	@Autowired
	private FilmeDAO dao;
	
	public Filme buscarFilme(int id) throws IOException{
		return dao.buscarFilme(id);
	}
	
	@Transactional
	public Filme inserirFilme(Filme filme) throws IOException {
		int id = dao.inserirFilme(filme);
		filme.setId(id);
		return filme;
	}
	
	@Transactional
	public void atualizarFilme(Filme filme) throws IOException {
		dao.atualizarFilme(filme);
	}
	
	@Transactional
	public void excluirFilme(int id) throws IOException {
		dao.excluirFilme(id);
	}

	public List<Filme> listarFilmes(String chave) throws IOException{
		return dao.listarFilmes(chave);
	}

	public List<Filme> listarFilmes() throws IOException{
		return dao.listarFilmes();
	}
	
	@Transactional
	public List<Filme> baixarFilmesMaisPopulares() throws IOException {
		RestTemplate rest = new RestTemplate();
		String url = BASE_URL + POPULAR + "?" + API_KEY + LANG;
		Populares resultado = rest.getForObject(url, Populares.class);
		List<Filme> filmes = new ArrayList<>();

		for (Movie movie : resultado.getResults()) {
			Filme filme = new Filme();
			filme.setTitulo(movie.getTitle());
			filme.setDataLancamento(movie.getRelease_date());
			filme.setPopularidade(movie.getPopularity());
			filme.setPosterPath(POSTER_URL + movie.getPoster_path());
			filme.setDescricao(movie.getOverview());
			Genero genero = new Genero();
			genero.setId(movie.getGenre_ids()[0]);
			filme.setGenero(genero);
			dao.inserirFilme(filme);
			filmes.add(filme);
		}

		return filmes;
	}
	
	@Transactional
	public List<Filme> baixarLancamentos() throws IOException {
		RestTemplate rest = new RestTemplate();
		String url = BASE_URL + UPCOMING + "?" + API_KEY + LANG;
		Lancamentos resultado = rest.getForObject(url, Lancamentos.class);
		List<Filme> filmes = new ArrayList<>();

		for (Movie movie : resultado.getResults()) {
			Filme filme = new Filme();
			filme.setTitulo(movie.getTitle());
			filme.setDataLancamento(movie.getRelease_date());
			filme.setPopularidade(movie.getPopularity());
			filme.setPosterPath(POSTER_URL + movie.getPoster_path());
			filme.setDescricao(movie.getOverview());
			Genero genero = new Genero();
			genero.setId(movie.getGenre_ids()[0]);
			filme.setGenero(genero);
			dao.inserirFilme(filme);
			filmes.add(filme);
		}

		return filmes;
	}
	
	@Transactional
	public void insereImagem(ServletContext servletContext, Filme filme, MultipartFile file) throws IOException {
		if(!file.isEmpty()) {
			BufferedImage src = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
			String path = servletContext.getRealPath(servletContext.getContextPath());
			path = path.substring(0, path.lastIndexOf(File.separatorChar));
			String nomeArquivo = "img" + filme.getId() + ".jpg";
			filme.setPosterPath(nomeArquivo);
			filme.setImagem(nomeArquivo);
			dao.atualizarFilme(filme);
			File destination = new File(path + File.separatorChar + "img" + File.separatorChar + nomeArquivo);
			if(destination.exists()) {
				destination.delete();
			}
			
			ImageIO.write(src, "jpg", destination);
		}
	}
}











