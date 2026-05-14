package io.spring.articleservice.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

// Read-only MyBatis mapper for querying all tags
@Mapper
public interface TagReadService {
  List<String> allTags();
}
