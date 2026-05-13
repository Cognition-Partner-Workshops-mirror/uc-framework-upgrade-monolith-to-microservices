package io.spring.article.infrastructure.mybatis.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

// MyBatis read-only mapper for fetching all tag names.
@Mapper
public interface TagReadService {
  List<String> all();
}
