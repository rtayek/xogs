import requests
import time

class OGSReviews:
    API_BASE = "https://online-go.com/api/v1"

    @staticmethod
    def get_json(url):
        """ Fetch JSON from the given URL, handling errors """
        try:
            response = requests.get(url)
            if response.status_code == 429:
                print("429 Too Many Requests - Sleeping...")
                time.sleep(5)
                return OGSReviews.get_json(url)  # Retry after sleep

            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            print("Error fetching data:", e)
            return None

    @staticmethod
    def list_all_reviews():
        """ List all reviews for all games on OGS """
        games_url = f"{OGSReviews.API_BASE}/games"
        games = OGSReviews.get_json(games_url)

        if not games or "results" not in games:
            print("No games found.")
            return

        print(f"Found {games.get('count', 0)} games.")
        next_page = games.get("next")

        while next_page:
            for game in games["results"]:
                game_id = game["id"]
                game_name = game.get("name", "Unknown")
                reviews_url = f"{OGSReviews.API_BASE}/games/{game_id}/reviews"

                reviews = OGSReviews.get_json(reviews_url)

                if not reviews or "results" not in reviews:
                    continue  # Skip games without reviews

                review_count = reviews.get("count", 0)
                if review_count == 0:
                    continue

                print(f"Game {game_id}: {game_name} has {review_count} reviews.")

                for review in reviews["results"]:
                    username = review.get("owner", {}).get("username")
                    print(f"\tReview by: {username}")

            games = OGSReviews.get_json(next_page)
            next_page = games.get("next") if games else None

    @staticmethod
    def main():
        """ Start listing all reviews on OGS """
        OGSReviews.list_all_reviews()

# Run the script
if __name__ == "__main__":
    OGSReviews.main()
    