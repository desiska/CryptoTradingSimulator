package com.trading.crypto.repository;

import com.trading.crypto.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class AppUserRepository implements RepositoryInterface<AppUser, Long>{
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public AppUserRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(AppUser entity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO users (username, password, email, balance) VALUES(?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, entity.getUsername());
                ps.setString(2, entity.getPassword());
                ps.setString(3, entity.getEmail());
                ps.setBigDecimal(4, entity.getBalance());

                return ps;
            }
        }, keyHolder);

        if (affectedRows > 0 && keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        } else {
            throw new RuntimeException("Failed to retrieve generated ID for user. Affected rows: " + affectedRows);
        }
    }

    @Override
    public int update(AppUser entity) {
        return jdbcTemplate.update("UPDATE users SET username=?, password=?, email=?, balance=? WHERE id=?",
                new Object[]{entity.getUsername(), entity.getPassword(), entity.getEmail(), entity.getBalance(), entity.getId()});
    }

    @Override
    public Optional<AppUser> findById(Long id) {
        try {
            AppUser user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE id=?",
                    BeanPropertyRowMapper.newInstance(AppUser.class), id);

            return Optional.ofNullable(user);
        }
        catch (IncorrectResultSizeDataAccessException e){
            return Optional.empty();
        }
    }

    public Optional<AppUser> findByUsername(String username) {
        try {
            AppUser user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE username=?",
                    BeanPropertyRowMapper.newInstance(AppUser.class), username);

            return Optional.ofNullable(user);
        }
        catch (IncorrectResultSizeDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<AppUser> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", BeanPropertyRowMapper.newInstance(AppUser.class));
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id);
    }

    @Override
    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM users");
    }
}
