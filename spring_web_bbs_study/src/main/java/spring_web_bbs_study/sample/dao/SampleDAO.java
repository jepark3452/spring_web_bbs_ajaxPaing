package spring_web_bbs_study.sample.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import spring_web_bbs_study.common.dao.AbstractDAO;

@Repository("sampleDAO")
public class SampleDAO extends AbstractDAO{

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectBoardList(Map<String, Object> commandMap) throws Exception {
		return (List<Map<String, Object>>)selectList("sample.selectBoardList", commandMap);
	}

}
