package io.spring.article.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

// MyBatis mapper for reading all available tags
@Mapper
public interface TagReadService {
  List<String> all();
}
