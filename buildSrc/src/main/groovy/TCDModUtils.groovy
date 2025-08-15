import org.gradle.api.Project

/**
 * TheCSDev's common utilities for Gradle Minecraft modding projects.
 */
final class TCDModUtils
{
	// ==================================================
	private TCDModUtils() {}
	// ==================================================
	/**
	 * From a {@link org.gradle.api.Project} instance, returns an array of filenames
	 * representing said project's Mixin files.
	 * @param project The project instance.
	 * @return A {@link String} array representing Mixin filenames.
	 */
	static String[] getMixinFileNames(Project project)
	{
		//first we obtain the path to the project's resources directory
		final File rssDir = new File(project.projectDir, "src/main/resources")

		//then we obtain a list of all direct child files in the directory
		//(note that it is nullable, meaning we have to handle null)
		final String[] rssFiles = rssDir.list()
		if(rssFiles == null) return new String[0]

		//then, from the file list, we only return the ones whose name contains '.mixin.'
		return rssFiles.toList()
				.findAll((String it) -> {
					it = it.toLowerCase(Locale.ENGLISH)
					return it.contains(".mixin") && it.endsWith(".json")
				})
				.toArray(new String[0])
	}
	// ==================================================
}
