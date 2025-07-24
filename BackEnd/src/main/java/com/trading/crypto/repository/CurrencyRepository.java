package com.trading.crypto.repository;

import com.trading.crypto.model.Currency;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class CurrencyRepository implements RepositoryInterface<Currency, Long>{
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public CurrencyRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(Currency entity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO user_currency (currency, quantity, user_id) VALUES(?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );

                ps.setString(1, entity.getCurrency());
                ps.setBigDecimal(2, entity.getQuantity());
                ps.setLong(3, entity.getUser_id());

                return ps;
            }
        }, keyHolder);

        if (affectedRows > 0 && keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        } else {
            throw new RuntimeException("Failed to retrieve generated ID for currency. Affected rows: " + affectedRows);
        }
    }

    @Override
    public int update(Currency entity) {
        return jdbcTemplate.update("UPDATE user_currency SET currency=?, quantity=? WHERE id=?",
                new Object[]{entity.getCurrency(), entity.getQuantity(), entity.getId()});
    }

    @Override
    public Optional<Currency> findById(Long id) {
        try {
            Currency currency = jdbcTemplate.queryForObject("SELECT * FROM user_currency WHERE id=?",
                    BeanPropertyRowMapper.newInstance(Currency.class), id);

            return Optional.ofNullable(currency);
        }
        catch (IncorrectResultSizeDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Currency> findAll() {
        return jdbcTemplate.query("SELECT * FROM user_currency", BeanPropertyRowMapper.newInstance(Currency.class));
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM user_currency WHERE id=?", id);
    }

    @Override
    public int deleteAll() {
        return jdbcTemplate.update("DELETE FROM user_currency");
    }

    public Optional<Currency> findByUserIDAndSymbol(Long userID, String currencySymbol) {
        try {
            Currency currency = jdbcTemplate.queryForObject("SELECT * FROM user_currency WHERE user_id=? AND currency=?",
                    BeanPropertyRowMapper.newInstance(Currency.class), userID, currencySymbol);

            return Optional.ofNullable(currency);
        }
        catch (IncorrectResultSizeDataAccessException e){
            return Optional.empty();
        }
    }

    public Collection<Currency> findByUserID(Long userID) {
        return jdbcTemplate.query("SELECT * FROM user_currency where user_id=?", BeanPropertyRowMapper.newInstance(Currency.class), userID);
    }

    public int deleteByUserID(Long userID) {
        return jdbcTemplate.update("DELETE FROM user_currency where user_id=?", userID);
    }
}
