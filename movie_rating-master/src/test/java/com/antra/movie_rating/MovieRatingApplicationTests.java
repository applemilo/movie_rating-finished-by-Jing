package com.antra.movie_rating;

import com.antra.movie_rating.api.request.RatingVO;
import com.antra.movie_rating.dao.MovieAverageScoreRepository;
import com.antra.movie_rating.dao.MovieRatingDAO;
import com.antra.movie_rating.dao.UserRepository;
import com.antra.movie_rating.dao.UserRoleRepository;
import com.antra.movie_rating.domain.Movie;
import com.antra.movie_rating.domain.MovieAverageScore;
import com.antra.movie_rating.domain.MovieRating;
import com.antra.movie_rating.domain.User;
import com.antra.movie_rating.service.MovieRatingService;
import com.antra.movie_rating.service.MovieRatingServiceImpl;
import com.antra.movie_rating.service.MovieService;
import com.antra.movie_rating.utility.MovieConverter;
import com.antra.movie_rating.utility.MovieScoreConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

//

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MovieRatingApplicationTests {

	@Autowired
	UserRoleRepository roleRepository;
	@Autowired
	UserRepository userRepository;

	@Autowired
	MovieService movieService;

	@Autowired
	MovieAverageScoreRepository avgScoreDAO;

	@Autowired
	MovieRatingDAO movieRatingDAO;

	@Autowired
	MovieRatingService movieRatingService;

	//@Test
	public void contextLoads() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("1234"));
	}
	@Test
	@Transactional
	public void testRoleDAO() {
		System.out.println(roleRepository.findAll().size());
	}

	@Test
	@Transactional
	public void testUserDAO() {
//		System.out.println(userRepository.existsByEmail("123@gmail.com"));
		System.out.println(userRepository.findByUsernameOrEmail("apple","").get());
	}


	@Test
	public void testGetMovieAverageScore() {
		MovieAverageScore score = movieService.getMovieAverageScoreById(1);
		System.out.println(score);
	}
	@Test
	@Transactional
	public void testSaveMovieAverageScore() {
		MovieAverageScore score = avgScoreDAO.updateAverage(30);
		System.out.println(score);
	}

	@Test
	public void testMovieConverter(){
		Movie m1= MovieConverter.MovieConverterByTitle("joker");

		Movie m2= MovieConverter.MovieConverterByTitle("joker");
		Movie m3= MovieConverter.MovieConverterByTitle("flight");

		System.out.println(m1);
		System.out.println(m2);
		System.out.println(m3);

	}
	@Test
	@Transactional
	public void testRatingService(){
		Map<Integer,Integer> rateStars=new HashMap<>();
		for (int i = 0; i < 5; i++) {
			rateStars.put(i+1,10);
		}
		RatingVO rate=new RatingVO();
		rate.setComment("great");
		rate.setMovieId(30);
		rate.setRateStars(rateStars);
		//System.out.println(rate);


		MovieRating mr=MovieScoreConverter.convertRatingVOtoMovieRating(rate, 28L);
		System.out.println(mr.getMovie());
//		System.out.println(mr.getScores());
//		System.out.println(mr.getUser());
		movieRatingService.saveRating(mr);

	}

	@Test

	public void testMyfeature(){
		System.out.println(movieService.searchMovie());
	}

}
