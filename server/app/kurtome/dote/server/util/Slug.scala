package kurtome.dote.server.util

object Slug {
  def apply(input: String): String = slugify(input)

  def slugify(input: String, maxPreSlugLength: Int = 50): String = {
    import java.text.Normalizer
    val normalized = Normalizer
      .normalize(input, Normalizer.Form.NFD)
      .replaceAll("[^\\w\\s-]", "") // Remove all non-word, non-space or non-dash characters
    normalized
      .substring(0, Math.min(maxPreSlugLength, normalized.length)) // Truncate to max length before trimming (in case we truncate at whitespace)
      .replace('-', ' ') // Replace dashes with spaces
      .trim // Trim leading/trailing whitespace (including what used to be leading/trailing dashes)
      .replaceAll("\\s+", "-") // Replace whitespace (including newlines and repetitions) with single dashes
      .toLowerCase // Lowercase the final results
  }
}
