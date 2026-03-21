package cn.abcyun.canguan.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDate convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        String value = source.trim();
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException ignore) {
            // 查询参数兼容前端传入的日期时间格式，业务内部仍统一按自然日处理。
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER).toLocalDate();
        } catch (DateTimeParseException ignore) {
            // 兼容可能出现的 ISO_LOCAL_DATE_TIME 格式，避免网关或前端编码差异导致绑定失败。
        }
        return LocalDateTime.parse(value, ISO_DATE_TIME_FORMATTER).toLocalDate();
    }
}
