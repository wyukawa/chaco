package chaco.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import chaco.TestBase;

public class MySQLServiceTest extends TestBase {

	MySQLService sut = getInjector().getInstance(MySQLService.class);
}
