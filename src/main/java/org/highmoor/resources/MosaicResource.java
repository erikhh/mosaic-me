package org.highmoor.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder
@Path("/mosaic/{x}/{y}/{z}")
@Produces("image/jpeg")
@Slf4j
public class MosaicResource {

	@GET
	public Response getMosaicTile(
			@PathParam("x") Integer x, 
			@PathParam("y") Integer y, 
			@PathParam("z") Integer z) {
		log.info("x {}, y {}, z {}", x, y, z);
		return Response.status(404).build();
	}
}
