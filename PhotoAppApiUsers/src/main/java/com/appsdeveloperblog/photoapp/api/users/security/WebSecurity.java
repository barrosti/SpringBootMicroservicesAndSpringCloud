package com.appsdeveloperblog.photoapp.api.users.security;

import javax.servlet.Filter;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.appsdeveloperblog.photoapp.api.users.service.UsersService;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
	
	private Environment env;
	private UsersService userService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public WebSecurity(Environment env, UsersService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
		super();
		this.env = env;
		this.userService = userService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		// env.getProperty("192.168.2.81")
		// http.authorizeHttpRequests().antMatchers("/users/**").permitAll();
		// http.authorizeHttpRequests().antMatchers("/users/**").permitAll().anyRequest().authenticated();
		http.authorizeHttpRequests().antMatchers("/users/**")
		.permitAll().anyRequest().authenticated()
		.and()
		.addFilter(getAuthenticationFilter());

		http.headers().frameOptions().disable();
	}


	private Filter getAuthenticationFilter() throws Exception {
		
		AuthenticationFilter authenticationFilter = new AuthenticationFilter(userService, env, authenticationManager());
		
		//authenticationFilter.setAuthenticationManager(authenticationManager());
		authenticationFilter.setFilterProcessesUrl( env.getProperty("login.url.path") );
		
		return authenticationFilter;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
	}
	
}
