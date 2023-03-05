package com.xu.community;

import com.xu.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
	@Autowired
	private SensitiveFilter sensitiveFilter;
	@Test
	public void testSensitiveFilter(){
		String text="这里可以赌博，嫖娼，开🤞票，🤞哈哈阿达,abcawwabc";
		text = sensitiveFilter.filter(text);
		System.out.println(text);
	}
}
