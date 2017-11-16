package org.highmoor;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import org.highmoor.resources.MosaicResource;

public class MosaicMeModule implements Module {

	@Override
	public void configure(Binder binder) {
		// Not much
	}

	@Provides
	public MosaicResource mosaicResource() {
		return MosaicResource.builder()
				.build();
	}
		
}
