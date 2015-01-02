package com.jmg.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String SECURE_ADMIN_PASSWORD = "rockandroll";
	
	@Autowired
	private SessionRepository<? extends ExpiringSession> sessionRepository;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterBefore(new SessionRepositoryFilter(sessionRepository), ChannelProcessingFilter.class)

			.csrf().disable()
			.formLogin()
				.loginPage("/login.html")
				.defaultSuccessUrl("/chat.html")
				.permitAll()
				.and()
			.logout()
				.logoutSuccessUrl("/login.html")
				.permitAll()
				.and()
			.authorizeRequests()
				.antMatchers("/js/**", "/lib/**", "/images/**", "/css/**").permitAll()
				.antMatchers("/stats").hasRole("ADMIN")
				.anyRequest().authenticated()
				.and();
				
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.authenticationProvider(new AuthenticationProvider() {
			
			public boolean supports(Class<?> authentication) {
				return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
			}
			
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
				
				return new UsernamePasswordAuthenticationToken(token.getName(), token.getCredentials(), 
							SECURE_ADMIN_PASSWORD.equals(token.getCredentials()) ? AuthorityUtils.createAuthorityList("ROLE_ADMIN") : null);
			}
		});
	}
}
